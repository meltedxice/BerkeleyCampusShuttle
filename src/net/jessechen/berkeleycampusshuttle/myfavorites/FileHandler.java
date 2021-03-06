/*******************************************************************************
 * Authors:
 *     Jesse Chen <contact@jessechen.net>
 * 
 * Copyright (c) 2011 Jesse Chen.
 * 
 * Berkeley Campus Shuttle is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Berkeley Campus Shuttle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Berkeley Campus Shuttle.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package net.jessechen.berkeleycampusshuttle.myfavorites;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

/**
 * @author Jesse Chen
 * 
 *         FileHandler handles read, write, and delete operations to
 *         favorites.dat
 * 
 */
public class FileHandler extends Activity {

	public final static String TOKEN = ",";
	private static ArrayList<String> myFavorites = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
	}

	/**
	 * The method readFile returns a String representation of the entire file.
	 * readFileWrapper encapsulates that method and parses the returned String
	 * into a neater String[].
	 * 
	 * This is also used prior to writing to the file to check if the specified
	 * stop is already in the file.
	 * 
	 * @param context
	 * @return a String[] representation of the file contents split by newline
	 * @throws FileNotFoundException
	 */
	public static String[] readFileWrapper(Context context)
			throws FileNotFoundException {
		String tempFavorites = null;
		String[] favorites = null;
		String[] trimFavorites = null;
		int toCopy;
		tempFavorites = readFile(context);
		favorites = tempFavorites.split("\n");
		// trim junk off
		toCopy = (favorites.length > 1) ? favorites.length - 1 : 0;
		trimFavorites = new String[toCopy];
		System.arraycopy(favorites, 0, trimFavorites, 0, toCopy);

		return trimFavorites;
	}

	/**
	 * checks if string data is in favorites.dat
	 * 
	 * @param c
	 * @param data
	 * @return true if string data is in favorites.dat, false if not found
	 * @throws IOException
	 */
	public static boolean inFavorites(Context c, String data) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(
					c.openFileInput("favorites.dat")));

			String currentLine;

			while ((currentLine = reader.readLine()) != null) {
				String trimmedLine = currentLine.trim();
				if (trimmedLine.equals(data)) {
					return true;
				}
			}

			reader.close();

		} catch (IOException e) {
			Toast.makeText(c, "Something bad happened...", Toast.LENGTH_SHORT)
					.show();
		}

		return false;
	}

	/**
	 * copies the file to a temp file except for the specified String, and at
	 * the end, it renames the temp file to the original.
	 * 
	 * @param c
	 * @param toDelete
	 *            string that you wish to delete from file
	 * @return true, if operation was successful
	 * @throws IOException
	 */
	public static boolean deleteLine(Context c, String toDelete) {
		try {
			File inputFile = new File(
					"/data/data/net.jessechen.berkeleycampusshuttle/files/favorites.dat");
			File tempFile = new File(
					"/data/data/net.jessechen.berkeleycampusshuttle/files/favorites.tmp");

			FileOutputStream fOut = c.openFileOutput("favorites.tmp",
					MODE_APPEND);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					c.openFileInput("favorites.dat")));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					fOut));

			String currentLine;

			while ((currentLine = reader.readLine()) != null) {
				String trimmedLine = currentLine.trim();
				if (trimmedLine.equals(toDelete)) {
					myFavorites.remove(currentLine);
					continue;
				}
				writer.write(currentLine);
				writer.newLine();
				writer.flush();
			}

			writer.close();
			reader.close();

			Toast.makeText(c, "Removed from Favorites", Toast.LENGTH_SHORT)
					.show();

			return tempFile.renameTo(inputFile);
		} catch (IOException e) {
			Toast.makeText(c, "Delete failed", Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	/**
	 * creates "favorites.dat" file if it doesn't exist, otherwise it takes the
	 * passed in data and appends it to the end of file.
	 * 
	 * @param context
	 * @param data
	 *            string to be written
	 */
	public static boolean writeToFile(Context context, String data) {
		FileOutputStream fOut = null;
		BufferedWriter writer = null;

		try {
			fOut = context.openFileOutput("favorites.dat", MODE_APPEND);
			writer = new BufferedWriter(new OutputStreamWriter(fOut));

			data.trim();

			// read file to see if data already exists
			String[] favorites = readFileWrapper(context);

			for (String favorite : favorites) {
				if (!myFavorites.contains(favorite)) {
					myFavorites.add(favorite);
				}
			}

			if (!myFavorites.contains(data)) {
				myFavorites.add(data);
				writer.write(data);
				writer.newLine();
				writer.flush();
				
				String forToast = data.replace(",", ": ");
				Toast.makeText(context,
						forToast + " has been added to your Favorites!",
						Toast.LENGTH_SHORT).show();
				
				return true;
			} else {
				Toast.makeText(context, "Already in your Favorites!",
						Toast.LENGTH_SHORT).show();
				return false;
			}
		} catch (FileNotFoundException f) {
			f.printStackTrace();
			Toast.makeText(context, "File not found", Toast.LENGTH_SHORT);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Error: did not save", Toast.LENGTH_SHORT)
					.show();
			return false;
		} finally {
			try {
				writer.close();
				fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Reads favorites.dat and outputs it in a String, use readFileWrapper to
	 * have it convert the string into a string array split by newline
	 * 
	 * @param c
	 * @return string representation of favorites.dat
	 * @throws FileNotFoundException
	 */
	protected static String readFile(Context c) throws FileNotFoundException {
		FileInputStream fIn = null;
		InputStreamReader isr = null;
		char[] inputBuffer = new char[255];
		String data = null;
		try {
			fIn = c.openFileInput("favorites.dat");
			isr = new InputStreamReader(fIn);
			isr.read(inputBuffer);
			data = new String(inputBuffer);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(c, "Something went wrong..", Toast.LENGTH_SHORT)
					.show();
		} finally {
			try {
				if (isr != null)
					isr.close();
				if (fIn != null)
					fIn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return data;
	}
}
