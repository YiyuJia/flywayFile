package idatamining.org;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

/**
 * Goal which generates a flyway SQL migration SQL script.
 *
 * @author yiyu jia
 * @goal generate
 * 
 */
public class MyMojo
    extends AbstractMojo
{
    /**
     * Location of the file.
     * 
     * @parameter expression="${flyway.SQL.directory}" default-value="."
     * @required
     */
    private File outputDirectory;
        
    
    /**
     * Location of the file name generate server.
     * @parameter expression="${flyway.filename.generator}"
     */
    private String nameServer;
    
    /**
     * File name.
     * @parameter expression="${myFilename}"
     */
    private String givenName;
    
    /**
     * flyway sql script file prefix
     * 
     * @parameter expression="${prefix}" default-value="V"
     */
    private String prefix;

    public void execute()
        throws MojoExecutionException
    {
        File f = outputDirectory;

        if ( !f.exists() )
        {
            f.mkdirs();
        }
        
        File migrationFile = null;

        FileWriter w = null;
        
        try
        {
        	migrationFile = getFileHandler(f);
        
        	//check if File exists already in any case. 
            w = new FileWriter( migrationFile );

            w.write( "---- Please write your migration SQL script here ------" );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error creating file " + migrationFile, e );
        }
        finally
        {
            if ( w != null )
            {
                try
                {
                    w.close();
                }
                catch ( IOException e )
                {
                    // ignore
                }
            }
        }
    }
    
    private File getFileHandler(File dir) throws IOException, MojoExecutionException{
    	File newFile;
    	String finalName;
    	if(nameServer == null){ //get file name based on local time
    		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
    		String dateStr = dateFormat.format(Calendar.getInstance().getTime());
    		finalName = prefix +  dateStr + "_" + getRandomNum() + "__" + givenName+".sql";
    		
    	}
    	else{ //get file name from a web service.     		
    		//send GET HTTP Request for file name. 
    		finalName = prefix +  readVersionNumberFromServer() + "__" + givenName+".sql"; 
    		
    	}
    	newFile = new File(dir, finalName);
    	newFile.createNewFile(); 
    	return newFile;    	
    }
    
    private String getRandomNum(){
    	Random r = new Random();     	
    	int R = r.nextInt(89) + 10; //not really random. but good enough
    	return Integer.toString(R);
    }
    
	private String readVersionNumberFromServer() throws MojoExecutionException {

		StringBuffer buffer = new StringBuffer();
		try {
			URL url = new URL(nameServer);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();

			InputStream stream = connection.getInputStream();
			// read the contents using an InputStreamReader

			InputStreamReader isr = new InputStreamReader(stream);
			Reader in = new BufferedReader(isr);
			int ch;
			while ((ch = in.read()) != 10) {
				buffer.append((char)ch);				
			}
			in.close();	
			return buffer.toString();
			//return s.substring(0, s.indexOf('?')-1);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoExecutionException(e.getMessage());
		}
	}
    
}



