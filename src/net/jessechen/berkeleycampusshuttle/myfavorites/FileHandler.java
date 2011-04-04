package net.jessechen.berkeleycampusshuttle.myfavorites;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

public class FileHandler extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    }
    
	public static void writeToFile(Context context, String data) {
		FileOutputStream fOut = null;
		OutputStreamWriter osw = null;
		BufferedWriter fbw = null;
		
		try {
			fOut = context.openFileOutput("favorites.dat", MODE_PRIVATE);
			osw = new OutputStreamWriter(fOut);
			fbw = new BufferedWriter(osw);
			
			fbw.append(data);
			fbw.newLine();
			fbw.flush();
			Toast.makeText(context, data + " added to your favorites", Toast.LENGTH_SHORT)
					.show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Error: did not save", Toast.LENGTH_SHORT)
					.show();
		} finally {
			try {
				fbw.close();
				osw.close();
				fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected static String readFile(Context context) {
		FileInputStream fIn = null;
		InputStreamReader isr = null;
		char[] inputBuffer = new char[255];
		String data = null;
		try {
			fIn = context.openFileInput("favorites.dat");
			isr = new InputStreamReader(fIn);
			isr.read(inputBuffer);
			data = new String(inputBuffer);
			Toast.makeText(context, "Favorites read", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Favorites not read", Toast.LENGTH_SHORT).show();
		} finally {
			try {
				isr.close();
				fIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}
}