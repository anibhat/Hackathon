package com.example.hackathon.controller;

import java.io.IOException;
import java.util.List;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.hackathon.model.Converter;
import com.example.hackathon.utility.ConverterUtil;

@RestController
public class ConverterController {
	
	@Autowired
	ConverterUtil util;
	
	@RequestMapping(path="/convert", method = RequestMethod.POST)
	public ResponseEntity<String> convert(@RequestBody Converter input)  {
		
	 if (input != null && input.getDir() != null) {
		List<String> fileList;
		try {
			fileList = util.listFilesAndFilesSubDirectories(input.getDir());
			if(fileList != null && fileList.size() > 0) {
				
			 //Spring Data support
			  if(util.checkForEntity(fileList))	{
				  return new ResponseEntity<String>("Application is not compatible for Spring Data", HttpStatus.BAD_REQUEST);
			  }
			  
			  if(util.checkForGradle(fileList)) {
				  return new ResponseEntity<String>("Application is not compatible for Gradle", HttpStatus.BAD_REQUEST);
			  }
			  
			  String checkForLambdaApp = util.checkForFileName(fileList, "StreamLambdaHandler.java");
			  if(checkForLambdaApp != null && !checkForLambdaApp.equalsIgnoreCase("")) {
				  return new ResponseEntity<String>("Already a spring boot Application", HttpStatus.BAD_REQUEST);
			  }
			   
			  
				
				String springBootAppPath = util.getSpringBootApplicationPath(fileList);
				
				if(springBootAppPath!= null && !springBootAppPath.equals("")) {
					util.updateSpringBootFile(springBootAppPath);
				} else {
					 return new ResponseEntity<String>("Please select a spring boot Application", HttpStatus.BAD_REQUEST);
				}
				
				String pomFilePath = util.checkForFileName(fileList, "pom.xml");
				if (pomFilePath != null && !pomFilePath.equalsIgnoreCase("")) {
					util.updatePomFile(pomFilePath);
					util.executeMaven(pomFilePath);
				}
			}
		} catch (IOException e) {
			return new ResponseEntity<String>("Error in updating file"+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (MavenInvocationException e) {
			return new ResponseEntity<String>("Error in Building the application"+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		  return new ResponseEntity<String>("updated successfully", HttpStatus.OK);
		}
		
		return null;
		
	}

}
