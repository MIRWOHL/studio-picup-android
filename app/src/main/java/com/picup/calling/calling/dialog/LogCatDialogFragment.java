package com.picup.calling.dialog;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.picup.calling.util.Logger;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.picup.calling.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.picup.calling.BuildConfig.LOGCAT_ENABLED;

public final class LogCatDialogFragment extends BaseDialogFragment {

    public static String Tag = "LogcatDialog";

    //View
    private Toolbar toolbar;
    private RecyclerView logRecyclerView;
    private RecyclerView controlRecyclerView;

    //Object
    private LogRecyclerViewAdapter logAdapter;
    private ControlRecyclerViewAdapter controlAdapter;
    private AsyncTask<Void, Void, List<Object>> getLogTask;
    private AsyncTask<Void, Void, URI> shareLogTask;

    //Value
    private ArrayList<String> srcList;
    private StringBuilder logTxtBuilder;
    private final String TagShare = " Share";
    private final String TagRefresh = "Refresh";
    private final String TagClear = "Clear";
    private final String TagDismiss = "Dismiss";
    private final int permissionRequestCode = 1000;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set style to force dialog to full screen
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Translucent_NoTitleBar);

        this.logAdapter = new LogRecyclerViewAdapter(getActivity());
        this.controlAdapter = new ControlRecyclerViewAdapter(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = null;
        if (inflater != null) {
            try{view = inflater.inflate(R.layout.dialog_logcat, container, false);}catch(Throwable t){}
        }
        if (view != null) {
            toolbar = (Toolbar) view.findViewById(R.id.logcat_toolbar);
            logRecyclerView = (RecyclerView) view.findViewById(R.id.logcat_log_view);
            controlRecyclerView = (RecyclerView) view.findViewById(R.id.logcat_control_view);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!LOGCAT_ENABLED) {
            remove();
            return;
        }

        if (toolbar != null) {
            toolbar.setTitle("Logcat");
            toolbar.setNavigationIcon(R.drawable.ic_white_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove();
                }
            });
        }
        if (logRecyclerView != null) {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            logRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            mLayoutManager.setStackFromEnd(true);
            logRecyclerView.setLayoutManager(mLayoutManager);

            logRecyclerView.setAdapter(logAdapter);
        }
        if (controlRecyclerView != null) {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            controlRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
            controlRecyclerView.setLayoutManager(mLayoutManager);

            controlRecyclerView.setAdapter(controlAdapter);
        }
        if (controlAdapter != null) {
            ArrayList<String> tagList = new ArrayList<>();
            tagList.add(TagRefresh);
            tagList.add(TagShare);
            tagList.add(TagClear);
            tagList.add(TagDismiss);
            controlAdapter.setTagList(tagList);
            controlAdapter.notifyDataSetChanged();
        }
        initGetLogTask();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getLogTask != null) {
            getLogTask.cancel(true);
        }
        if (shareLogTask != null) {
            shareLogTask.cancel(true);
        }
    }

    private synchronized void initGetLogTask() {
        if (!LOGCAT_ENABLED) {
            return;
        }
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing() || isRemoving()) {
            return;
        }
        if (getLogTask != null) {
            return;
        }
        GetLogTask newTask = new GetLogTask(activity);
        newTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        getLogTask = newTask;
    }

    /**
     * Method to clear logcat
     */
    private synchronized void initClearLog(boolean getLog) {
        if (!LOGCAT_ENABLED) {
            return;
        }
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing() || isRemoving()) {
            return;
        }
        Runtime runtime = Runtime.getRuntime();
        if (runtime != null) {
            try {
                runtime.exec("logcat -c");
            } catch (Throwable t) {
                Logger.logThrowable(t);
            }
        }
        if (getLog) {
            initGetLogTask();
        }
    }

    /**
     * Method to share log
     */
    private synchronized void initShareLog() {
        String log = "LogCatDialogFragment - initShareLog";
        if (!LOGCAT_ENABLED) {
            log += " - not enable";
            Logger.log(log);
            return;
        }
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing() || isRemoving()) {
            log += " - invalid state";
            Logger.log(log);
            return;
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "No Write To External Storage Permission", Toast.LENGTH_SHORT).show();
            log += " - No Write to External Storage Permission";
            Logger.log(log);
            String[] permissionList = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissionList, permissionRequestCode);
            return;
        }
        if (logTxtBuilder == null || shareLogTask != null) {
            log += " - builder null or task not null";
            Logger.log(log);
            return;
        }
        String title = "PICUP_APP_QA_log - LogCat";
        String body = logTxtBuilder.toString();
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(body)) {
            log += " - title or body empty";
            Logger.log(log);
            return;
        }
        ShareLogTask newTask = new ShareLogTask(activity, title, "PICUP_QA_log", body);
        newTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        shareLogTask = newTask;
    }

    private class GetLogTask extends AsyncTask<Void, Void, List<Object>> {
        private Context context;
        GetLogTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onCancelled() {
            getLogTask = null;
        }

        @Override
        protected void onPostExecute(List<Object> objects) {
            getLogTask = null;
            onHandleLogRetrieved(objects);
        }

        @Override
        protected List<Object> doInBackground(Void... params) {
            if (isCancelled() || context == null) {
                return null;
            }
            String filter = "";
            try {
                filter = context.getString(R.string.app_logcat_filter);
            } catch (Throwable t) {
            }
            String logcat_command = null;
            if (!TextUtils.isEmpty(filter)) {
                logcat_command = "logcat -dv time -s " + filter;
            } else {
                logcat_command = "logcat -dv time";
            }
            Runtime runTime = Runtime.getRuntime();
            Process process = null;
            BufferedReader bufferedReader = null;
            InputStream inputStream = null;
            if (runTime != null) {
                try {
                    process = runTime.exec(logcat_command);
                } catch (Throwable t) {
                    Logger.logThrowable(t);
                }
            }
            if (process != null) {
                inputStream = process.getInputStream();
                InputStreamReader inputStreamReader = null;
                if (inputStream != null) {
                    inputStreamReader = new InputStreamReader(inputStream);
                }
                if (inputStreamReader != null) {
                    bufferedReader = new BufferedReader(inputStreamReader);
                }
            }

            if (bufferedReader == null) {
                return null;
            }
            ArrayList<String> logList = new ArrayList<>();
            StringBuilder logTextBuilder = new StringBuilder();

            String appVersionString = getAppVersionString(context);
            if (!TextUtils.isEmpty(appVersionString)) {
                logList.add(appVersionString);
                logTextBuilder.append(appVersionString);
            }

            String line = "";
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    if (!TextUtils.isEmpty(line)) {
                        logList.add(line);
                        logTextBuilder.append(line + "\n");
                    }
                    if (isCancelled()) {
                        break;
                    }
                }
            } catch (Throwable t) {
                Logger.logThrowable(t);
            }
            List<Object> result = new ArrayList<>();
            result.add(logList);
            result.add(logTextBuilder);
            return result;
        }
    }

    /**
     * Task to create attachement file
     */
    private class ShareLogTask extends AsyncTask<Void, Void, URI> {
        private Context context;
        private String emailTitle;
        private String fileName;
        private String bodyText;

        ShareLogTask(Context context, String emailTitle, String fileName, String bodyText) {
            this.context = context;
            this.emailTitle = emailTitle;
            this.fileName = fileName;
            this.bodyText = bodyText;
        }

        @Override
        protected URI doInBackground(Void... params) {
            if (isCancelled() || context == null) {
                return null;
            }
            if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(bodyText)) {
                return null;
            }
            URI result = null;
            try {
                // locat file
                File externalStorageDir = Environment.getExternalStorageDirectory();
                File myFile = new File(externalStorageDir, fileName + ".txt");

                // delete previous file
                if (myFile.exists()) {
                    myFile.delete();
                }
                // create new file
                if (!myFile.exists()) {
                    myFile.createNewFile();
                    if (myFile.exists()) {
                        FileOutputStream fOut = null;
                        OutputStreamWriter myOutWriter = null;
                        try {
                            fOut = new FileOutputStream(myFile);
                            myOutWriter = new OutputStreamWriter(fOut);
                            myOutWriter.append(bodyText);
                            result = myFile.toURI();
                        } catch (IOException io) {
                        } catch (Throwable t) {
                            Logger.logThrowable(t);
                        } finally {
                            if (myOutWriter != null) {
                                myOutWriter.flush();
                                myOutWriter.close();
                            }
                            if (fOut != null) {
                                fOut.close();
                            }
                        }
                    }
                }
            } catch (IOException io) {

            } catch (Throwable t) {
                Logger.logThrowable(t);
            }
            return result;
        }

        @Override
        protected void onCancelled() {
            shareLogTask = null;
        }

        @Override
        protected void onPostExecute(URI fileURI) {
            shareLogTask = null;
            if (fileURI == null) {
                return;
            }
            Activity activity = getActivity();
            if (activity == null || activity.isFinishing() || isRemoving()) {
                return;
            }
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

            // Add data to the intent, the receiving app will decide what to do with it.
            intent.putExtra(Intent.EXTRA_SUBJECT, emailTitle);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(fileURI.toString()));
            Calendar c = Calendar.getInstance();
            intent.putExtra(Intent.EXTRA_TEXT, c.getTime().toString());
            try {
                activity.startActivity(Intent.createChooser(intent, "How do you want to share?"));
            } catch (ActivityNotFoundException ane) {
                Logger.logThrowable(ane);
            }
        }
    }

    private synchronized void onHandleLogRetrieved(List<Object> results) {
        if (results == null || results.isEmpty()) {
            return;
        }
        srcList = null;
        logTxtBuilder = null;
        if (results.size() == 2) {
            srcList = (ArrayList<String>) results.get(0);
            logTxtBuilder = (StringBuilder) results.get(1);
        }
        if (logAdapter != null) {
            logAdapter.setLogList(srcList);
            logAdapter.notifyDataSetChanged();
        }
    }

    private String getAppVersionString(Context context) {
        if (context == null) {
            return null;
        }
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        if (packageManager == null || TextUtils.isEmpty(packageName)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Package Name:");
        sb.append(packageName);

        PackageInfo pInfo = null;
        String version = null;
        int versionCode = 0;
        try {
            pInfo = packageManager.getPackageInfo(packageName, 0);
        } catch (Throwable t) {
        }
        if (pInfo != null) {
            version = pInfo.versionName;
            versionCode = pInfo.versionCode;
        }
        sb.append("\n");
        sb.append(" - version:");
        sb.append(version);
        sb.append("\n");
        sb.append(" - versionCode:");
        sb.append(versionCode);

        return sb.toString();
    }

    private class LogRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<String> logList;
        private Context context;

        LogRecyclerViewAdapter(Context context) {
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = null;
            if (context != null) {
                inflater = LayoutInflater.from(context);
            }
            View view = null;
            if (inflater != null) {
                try {
                    view = inflater.inflate(R.layout.itm_log, parent, false);
                } catch (Throwable t) {
                }
            }
            LogViewHolder viewHolder = null;
            if (view != null) {
                viewHolder = new LogViewHolder(view);
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            LogViewHolder viewHolder = null;
            if (holder != null && holder instanceof LogViewHolder) {
                viewHolder = (LogViewHolder) holder;
            }
            String log = null;
            if (logList != null) {
                if (position  >= 0 && position < logList.size()) {
                    log = logList.get(position);
                }
            }
            if (viewHolder == null || TextUtils.isEmpty(log)) {
                return;
            }
            if (viewHolder.logTextView != null) {
                viewHolder.logTextView.setText(log);
            }
        }

        @Override
        public int getItemCount() {
            if (logList != null) {
                return logList.size();
            }
            return 0;
        }

        void setLogList(ArrayList<String> logList) {
            this.logList = logList;
        }

        private class LogViewHolder extends RecyclerView.ViewHolder {
            private TextView logTextView;

            LogViewHolder(View itemView) {
                super(itemView);
                logTextView = (TextView) itemView.findViewById(R.id.log_txt);
            }
        }
    }

    private class ControlRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<String> tagList;
        private Context context;
        private ItemClickManager itemClickManager = new ItemClickManager();
        ControlRecyclerViewAdapter(Context context) {
            this.context = context;
        }
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = null;
            if (context != null) {
                inflater = LayoutInflater.from(context);
            }
            View view = null;
            if (inflater != null) {
                try {
                    view = inflater.inflate(R.layout.itm_log_control, parent, false);
                } catch (Throwable t) {
                }
            }
            ControlViewHolder viewHolder = null;
            if (view != null) {
                viewHolder = new ControlViewHolder(view);
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ControlViewHolder viewHolder = null;
            if (holder != null && holder instanceof ControlViewHolder) {
                viewHolder = (ControlViewHolder) holder;
            }
            String tag = null;
            if (tagList != null) {
                if (position >= 0 && position < tagList.size()) {
                    tag = tagList.get(position);
                }
            }
            if (viewHolder == null || TextUtils.isEmpty(tag)) {
                return;
            }
            ItemOnClickListener itemOnClickListener = new ItemOnClickListener(tag);
            if (viewHolder.button != null) {
                viewHolder.button.setText(tag);
                viewHolder.button.setOnClickListener(itemOnClickListener);
            }
        }

        private class ItemOnClickListener implements View.OnClickListener {
            private String tag;
            ItemOnClickListener(String tag) {
                this.tag = tag;
            }
            @Override
            public void onClick(View v) {
                if (itemClickManager == null) {
                    return;
                }
                itemClickManager.onClickEvent(v, tag);
            }
        }

        private class ItemClickManager {
            final long MIN_CLICK_INTERVAL = 200;

            /**
             * last click time stamp
             */
            long mLastClickTime;

            /**
             * Constructor
             */
            ItemClickManager() {
            }

            /**
             * abstract class for implement to define
             *
             * @param v The view that was clicked.
             */
            void onSingleClickEvent(View v, String tag) {
                if (v == null || TextUtils.isEmpty(tag)) {
                    return;
                }
                if (tag.equals(TagRefresh)) {
                    initGetLogTask();
                } else if (tag.equals(TagDismiss)) {
                    remove();
                } else if (tag.equals(TagShare)) {
                    initShareLog();
                } else if (tag.equals(TagClear)) {
                    initClearLog(true);
                }
            }

            void onClickEvent(View v, String tag) {
                long currentClickTime= SystemClock.uptimeMillis();
                long elapsedTime=currentClickTime-mLastClickTime;
                mLastClickTime=currentClickTime;

                // check desire interval first then minimum click interval
                if (elapsedTime<=MIN_CLICK_INTERVAL) {
                    return;
                }

                onSingleClickEvent(v, tag);
            }
        }

        @Override
        public int getItemCount() {
            if (tagList != null) {
                return tagList.size();
            }
            return 0;
        }

        void setTagList(ArrayList<String> tagList) {
            this.tagList = tagList;
        }

        private class ControlViewHolder extends RecyclerView.ViewHolder {
            private Button button;

            ControlViewHolder(View itemView) {
                super(itemView);
                button = (Button) itemView.findViewById(R.id.log_control_button);
            }
        }
    }
}