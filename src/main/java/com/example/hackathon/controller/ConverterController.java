package com.example.hackathon.controller;

import java.io.IOException;
import java.util.List;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.hackathon.model.Git;
import com.example.hackathon.utility.ConverterUtil;

import io.swagger.annotations.Api;

@RestController

@RequestMapping("/api/v1")
@Api(value = "Serverless migrator system", description = "Application to convert Spring boot to Lambda")
public class ConverterController {
	
	@Autowired
	ConverterUtil util;
	
	//@RequestMapping(path="/convert", method = RequestMethod.POST)
	@PostMapping("/convert")
	public ResponseEntity<String> convert(@RequestBody Git input) throws IOException  {

	 if (input != null && input.getRepository() != null && !input.getRepository().equals("")) {
		List<String> fileList;
		String path = "";
		try {
			path = util.cloneRepository(input.getRepository(), input.getUserName(), input.getPassword());
	
			fileList = util.listFilesAndFilesSubDirectories(path);
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
				util.updateSam();
				
				/*util.fileList.clear();
				if (path != null && !path.equals("")) {
					util.deleteDir(path);
				}*/
			}
		} catch (IOException e) {
			return new ResponseEntity<String>("Error in updating file"+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (MavenInvocationException e) {
			return new ResponseEntity<String>("Error in Building the application"+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (InvalidRemoteException e) {
			return new ResponseEntity<String>("Error in downloading"+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (TransportException e) {
			return new ResponseEntity<String>("Error in downloading"+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (GitAPIException e) {
			return new ResponseEntity<String>("Error in downloading"+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		  return new ResponseEntity<String>("updated successfully", HttpStatus.OK);
		}
		
		return null;
		
	}

}
