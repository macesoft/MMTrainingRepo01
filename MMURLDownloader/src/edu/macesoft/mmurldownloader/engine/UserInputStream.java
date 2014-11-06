package edu.macesoft.mmurldownloader.engine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class UserInputStream 
{
	private BufferedInputStream sourceURLBuffered = null;
	private BufferedOutputStream destinationFileBuffered = null;
	private UserInputPOJO userInputPOJO = null;
	
	public UserInputStream(UserInputPOJO userInputPOJO)
	{
		super();
		this.userInputPOJO = userInputPOJO;
	}
	
	public BufferedInputStream getSourceStream() throws MalformedURLException, IOException 
	{
		if (sourceURLBuffered == null)
		{
			URL aURL = null;
			aURL = new URL(userInputPOJO.getSourceUrl());
			sourceURLBuffered = new BufferedInputStream(aURL.openConnection().getInputStream());
		}
		return sourceURLBuffered;
	}

	public BufferedOutputStream getDestinationStream() throws FileNotFoundException
	{
		if (destinationFileBuffered == null)
		{
			destinationFileBuffered = new BufferedOutputStream(new FileOutputStream(userInputPOJO.getDestinationFile()));
		}
		return destinationFileBuffered;
	}
	
	public void closeStreams() throws IOException 
	{
		sourceURLBuffered.close();
		destinationFileBuffered.close();
	}
}
