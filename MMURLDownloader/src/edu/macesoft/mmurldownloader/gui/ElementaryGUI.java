package edu.macesoft.mmurldownloader.gui;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import edu.macesoft.mmurldownloader.engine.*;

public abstract class ElementaryGUI
{
	static public void execute()
	{
		BufferedReader userInputData = new BufferedReader(new InputStreamReader(System.in));
		UserInputPOJO inputData = new UserInputPOJO();
		
		try
		{
			System.out.println(">> Enter source URL file        :");			
			inputData.setSourceUrl(userInputData.readLine());
			
			System.out.println(">> Enter destination local file :");
			inputData.setDestinationFile(userInputData.readLine());
			
			System.out.println(">> Proceed (Y/N)? :");
			if (userInputData.readLine().compareToIgnoreCase("Y") == 0)
			{
				Downloader aDownloader = new Downloader(new UserInputStream(inputData));
				System.out.println(aDownloader.download() + " bytes read.");
				
				
			}
		}
		catch (MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
