package edu.macesoft.mmurldownloader.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

public class Downloader
{
	UserInputStream endpointStreams = null;

	public Downloader(UserInputStream endpointStreams)
	{
		super();
		this.endpointStreams = endpointStreams;
	}
	
	public long download() throws MalformedURLException, FileNotFoundException, IOException
	{
		int data = 0;
		long counter = 0;
		while ((data = endpointStreams.getSourceStream().read()) != (-1))
		{
			counter++;
			endpointStreams.getDestinationStream().write(data);
		}
		endpointStreams.closeStreams();
		
		return counter;
	}
}
