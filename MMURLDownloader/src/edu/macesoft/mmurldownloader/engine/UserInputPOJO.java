/**
 * 
 */
package edu.macesoft.mmurldownloader.engine;


/**
 * @author Marcelo
 * This class contains both the source and destination addresses for the download process 
 */
public class UserInputPOJO
{
	private String sourceURL = null;
	private String destinationFile = null;

	public UserInputPOJO()
	{
		super();
	}
	
	public UserInputPOJO(String sourceURL, String destinationFile)
	{
		this.sourceURL = sourceURL;
		this.destinationFile = destinationFile;
	}
	
	public String getSourceUrl()
	{
		return sourceURL;
	}
	
	public void setSourceUrl(String sourceURL)
	{
		this.sourceURL = sourceURL;
	}
	
	public String getDestinationFile()
	{
		return destinationFile;
	}
	
	public void setDestinationFile(String destinationFile)
	{
		this.destinationFile = destinationFile;
	}
}