package net.jessechen.berkeleycampusshuttle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.jessechen.berkeleycampusshuttle.myfavorites.FileHandler;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Stop is the activity that shows the user the next three predictions for the
 * specified route and stop. This is the final destination for the user. From
 * here, the user can add the specified stop to their favorites for quicker
 * access.
 * 
 * @author Jesse Chen
 * 
 */
public class Stop extends Activity {
	private static Bundle b;
	private static CharSequence busStop, routeName;
	private static Calendar calendar;
	private static TextView title, countdown1, countdown2, countdown3;
	private static int[][] result;
	private static int hourRemaining, minuteRemaining, curHour, curMinute,
			dayOfWeek;
	private MyCount counter1, counter2, counter3;
	private static Button addToFavButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.stop);

		b = getIntent().getExtras();
		busStop = b.getCharSequence("stop");
		routeName = b.getCharSequence("route");

		title = (TextView) findViewById(R.id.t_stop);
		title.setText(routeName + ": Predictions for " + busStop);

		countdown1 = (TextView) findViewById(R.id.countdown1);
		countdown2 = (TextView) findViewById(R.id.countdown2);
		countdown3 = (TextView) findViewById(R.id.countdown3);

		calculate();

		addToFavButton = (Button) findViewById(R.id.b_addtofav);
		addToFavButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(Stop.this, FileHandler.class));
				FileHandler.writeToFile(getApplicationContext(), routeName
						+ "," + busStop);
			}
		});
	}

	/**
	 * This method takes care of the three CountDownTimers by pulling data from
	 * the XML file and initializing them. Also takes care of cases when there
	 * are no more predictions for the day , and when user is viewing this on a
	 * weekend (campus shuttle does not run on weekends).
	 * 
	 */
	public void calculate() {
		countdown1.invalidate(); // do i need this
		countdown2.invalidate();
		countdown3.invalidate();

		calendar = Calendar.getInstance();
		curHour = calendar.get(Calendar.HOUR_OF_DAY);
		curMinute = calendar.get(Calendar.MINUTE);
		dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

		if (dayOfWeek != Calendar.SUNDAY && dayOfWeek != Calendar.SATURDAY) {
			try {
				result = getEventsFromAnXML(this, b.getInt("xml"), busStop);
			} catch (XmlPullParserException e) {
				Toast.makeText(getApplicationContext(),
						"A fatal error occured.  Report it!",
						Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(),
						"A fatal error occured.  Report it!",
						Toast.LENGTH_SHORT).show();
			}

			if (result[0][0] != -1) { // first hour result is -1, no more
										// predictions for the day
				if (result[0][0] != -1) {
					hourRemaining = (result[0][0] - curHour);
					minuteRemaining = (result[0][1] - curMinute);
					counter1 = new MyCount(countdown1, 0, hourRemaining
							* 3600000 + minuteRemaining * 60000, 1000);
					counter1.start();
				} else {
					countdown1.setText("");
				}
				if (result[1][0] != -1) {
					hourRemaining = (result[1][0] - curHour);
					minuteRemaining = (result[1][1] - curMinute);
					counter2 = new MyCount(countdown2, 1, hourRemaining
							* 3600000 + minuteRemaining * 60000, 1000);
					counter2.start();
				} else {
					countdown2.setText("");
				}
				if (result[2][0] != -1) {
					hourRemaining = (result[2][0] - curHour);
					minuteRemaining = (result[2][1] - curMinute);
					counter3 = new MyCount(countdown3, 2, hourRemaining
							* 3600000 + minuteRemaining * 60000, 1000);
					counter3.start();
				} else {
					countdown3.setText("");
				}
			} else {
				countdown1.setText("No more predictions for the day");
			}
		} else {
			countdown1
					.setText("Campus shuttle does not run during the weekends");
		}
	}

	/**
	 * Custom CountDownTimer that displays how many minutes remaining along with
	 * the estimated arrival time. Switches the text to "Arriving" when it is
	 * under a minute from prediction.
	 * 
	 * When the CountDownTimer finishes, it cancels all 3 CountDownTimers and
	 * calls calculate() again to recalculate the next three predictions.
	 * 
	 * @author Jesse Chen
	 * 
	 */
	public class MyCount extends CountDownTimer {
		private TextView tv;
		private String subString, minute, hour, ampm;

		public MyCount(TextView cd, int index, long millisInFuture,
				long countDownInterval) {
			super(millisInFuture, countDownInterval);
			tv = cd;
			minute = Integer.toString(result[index][1]);
			if (minute.length() == 1) { // append extra 0 for formatting
				if (minute == "0") {
					minute = "00";
				} else {
					minute = "0" + minute;
				}
			}
			hour = Integer.toString(result[index][0] % 12); // add am and pm
			if (hour == "0") {
				hour = "12";
			}
			if (result[index][0] > 12) {
				ampm = "pm";
			} else {
				ampm = "am";
			}
			subString = " (at " + hour + ":" + minute + ampm + ")";
		}

		@Override
		public void onFinish() {
			countdown1.setText("");
			if (counter1 != null) {
				counter1.cancel();
			}
			countdown2.setText("");
			if (counter2 != null) {
				counter2.cancel();
			}
			countdown3.setText("");
			if (counter3 != null) {
				counter3.cancel();
			}
			calculate();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			int timeRemaining = (int) millisUntilFinished / 60000;
			if (timeRemaining == 0) {
				tv.setText("Arriving" + subString);
			} else {
				tv.setText(timeRemaining + " minutes remaining" + subString);
			}
		}
	}

	/**
	 * Handles the XML parsing and returns the next three predictions for a
	 * specified stop.
	 * 
	 * I used a SAXParser for my app because I read that SAXParsers are more
	 * efficient than DOMParsers since SAX only has to run through the XML file
	 * once. I aim to keep it fast (even if it is negligible) by design choice.
	 * 
	 * @param activity
	 * @param xml
	 * @param stop
	 *            name of the specified stop
	 * @return int[3][2] array that contains three predictions, with each
	 *         prediction containing the hour value, then the minute value in
	 *         that order.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private int[][] getEventsFromAnXML(Activity activity, int xml,
			CharSequence stop) throws XmlPullParserException, IOException {
		InputStream istream = null;
		int[][] result = null;
		try {
			istream = activity.getResources().openRawResource(xml);
			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader */
			SaxyParser myExampleHandler = new SaxyParser();
			xr.setContentHandler(myExampleHandler);
			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(istream));
			/* Parsing has finished. */
			result = SaxyParser.getResult();
		} catch (Exception FileNotFoundException) {
			FileNotFoundException.printStackTrace();
		}
		return result;
	}

	/*
	 * We want to make sure that when the activity calls onStop() it also stops
	 * the CountDownTimers running in the background.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	public void onStop() {
		super.onStop();
		if (counter1 != null) {
			counter1.cancel();
		}
		if (counter2 != null) {
			counter2.cancel();
		}
		if (counter3 != null) {
			counter3.cancel();
		}
	}

	public static CharSequence getBusStop() {
		return busStop;
	}

	public static CharSequence getRouteName() {
		return routeName;
	}

	public static int getCurHour() {
		return curHour;
	}

	public static int getCurMinute() {
		return curMinute;
	}
}