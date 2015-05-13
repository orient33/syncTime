package cn.yu.dun;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SyncTime extends Activity {

	final DateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss",Locale.CHINA);
	final DateFormat cmddf = new SimpleDateFormat("yyyyMMdd.HHmmss");
	final int RefreshGap=5000;
	static final int Msg_Tick_Time=1;
	TextView netTimeV,localTimeV;
	Button mSetButton,mUpdateButton;
	long mNetTime=0;
	private static final String URL = "http://www.baidu.com";
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Msg_Tick_Time:
				refreshTime();
				sendEmptyMessageDelayed(Msg_Tick_Time, RefreshGap);
				mNetTime += RefreshGap;
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		netTimeV = (TextView) findViewById(R.id.netTime);
		localTimeV = (TextView) findViewById(R.id.localTime);
		mSetButton=(Button)findViewById(R.id.setTime);
		mUpdateButton= (Button)findViewById(R.id.updateNetTime);
		 new NetTimeTask().execute(null,null,null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshTime();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public void buttonClick(View v){
		if (v == mUpdateButton) {
//			if(android.os.Process.)
			mHandler.removeMessages(Msg_Tick_Time);
			 new NetTimeTask().execute(null,null,null);
		} else if (v == mSetButton) {
			upgradeRootPermission(mNetTime);
		}
	}
	private void refreshTime() {
		localTimeV.setText(df.format(new Date(System.currentTimeMillis())));
		if (0 == mNetTime)
			netTimeV.setText(R.string.geting);
		else
			netTimeV.setText(df.format(new Date(mNetTime)));
	}

	private long updateNetTime() {
		long now = 0;
		try {
			URLConnection uc = new URL(URL).openConnection();
			uc.connect();
			now = uc.getDate();
		} catch (IOException e) {
		}
		return now;
	}

	class NetTimeTask extends AsyncTask<Object, Object, Long> {

		@Override
		protected void onPreExecute(){
			mUpdateButton.setEnabled(false);
		}
		
		@Override
		protected Long doInBackground(Object... arg0) {
			return updateNetTime();
		}
		@Override
		protected void onPostExecute(Long result) {
			mNetTime = result;
			mHandler.sendEmptyMessage(Msg_Tick_Time);
			mUpdateButton.setEnabled(true);
		}

	};

	private boolean upgradeRootPermission(long n) {
		String pkgCodePath = getPackageCodePath();
		Process process = null;
		DataOutputStream os = null;
		try {
			String sss=cmddf.format(new Date(n));
			String cmd = "chmod 777 " + pkgCodePath;
			cmd = "date  -s  "+sss;
			process = Runtime.getRuntime().exec("su"); // 切换到root帐号
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
			Toast.makeText(this, "su: " + cmd, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
		return true;
	}
}
