package com.example.hackathon.utility;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

@Service
public class ConverterUtil {
	
	@Value("${pathToClone}")
	public String pathToSave;
	
	@Value("${mavenInstallationPath}")
	public String mavenInstallationPath;
	
	public static List<String> fileList = new ArrayList<String>();
	
	 /**
     * List all files from a directory and its subdirectories
     * @param directoryName to be listed
	 * @throws IOException 
     */
    public List<String> listFilesAndFilesSubDirectories(String directoryName) throws IOException{
        File directory = new File(directoryName);
        //get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList){
            if (file.isFile()){
            	fileList.add(file.getAbsolutePath());
            } else if (file.isDirectory()){
                listFilesAndFilesSubDirectories(file.getAbsolutePath());
            }
        }
        return fileList;
    }

	public static List<String> getFileList() {
		return fileList;
	}

	public static void setFileList(List<String> fileList) {
		ConverterUtil.fileList = fileList;
	}
	
	
    
	public String getPathToSave() {
		return pathToSave;
	}

	public void setPathToSave(String pathToSave) {
		this.pathToSave = pathToSave;
	}

	public String getSpringBootApplicationPath(List<String> fileList) throws IOException {
		for(String st : fileList) {
			if (st.endsWith(".java")) {
				long count = Files.lines(Paths.get(st))
       			     .filter(s -> s.contains("@SpringBootApplication"))
       			     .count();
			   if(count > 0) {
				   return st;
			   }
			}
		}
		return "";
	}
	
	public boolean checkForEntity(List<String> fileList) throws IOException {
		for(String st : fileList) {
			if (st.endsWith(".java")) {
				long count = Files.lines(Paths.get(st))
       			     .filter(s -> s.contains("@Entity"))
       			     .count();
			   if(count > 0) {
				   return true;
			   }
			}
		}
		return false;
	}
	
	
	public boolean checkForGradle(List<String> fileList) throws IOException {
		for(String st : fileList) {
			if (st.endsWith(".gradle")) {
				   return true;
			   }
		}
		return false;
	}
	
	
	public String checkForFileName(List<String> fileList, String fileName) throws IOException {
		for(String st : fileList) {
			if (st.endsWith(fileName)) {
				return st;
			}
		}
		return "";
	}
	
	
	public void updateSpringBootFile(String filePath) throws IOException {
		    String[] fileNames = filePath.split(Pattern.quote(File.separator));
			String fileName = fileNames[fileNames.length - 1];
			String appNames[] = fileName.split(".java");
			Path path = Paths.get(filePath);
			Charset charset = StandardCharsets.UTF_8;
			
			String content = new String(Files.readAllBytes(path), charset);
   			if (content.startsWith("package")) {
   				int index = content.indexOf(";");
   				String sub = content.substring(0, index+1);
   				String[] packageNames = sub.split("package ");
   				String packageName = packageNames[1];
   				StringBuffer sb = new StringBuffer(content);
   				sb.insert(index+2, "\r\n"+"import org.springframework.boot.web.support.SpringBootServletInitializer;");
   				content = sb.toString();
   				
   				/*Resource res = new ClassPathResource("StreamLambdaHandler.txt");
   		        File myFile = res.getFile();
   				final byte[] bytes = Files.readAllBytes(Paths.get(myFile.getPath()));
   				String fileContent = new String(bytes, charset);*/
   				
   				String fileContent = "";
   				ClassPathResource cpr = new ClassPathResource("StreamLambdaHandler.txt");
   				byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
   				fileContent = new String(bdata, StandardCharsets.UTF_8);
   				
   				
   				fileContent = fileContent.replaceAll("package", "package "+packageName);
   				fileContent = fileContent.replaceAll("SpringBootDemoApplication12356", appNames[0]);
   				String newPath = filePath.replaceAll(appNames[0]+".java", "StreamLambdaHandler.java");
   				Files.write(Paths.get(newPath), fileContent.getBytes(charset));
    			}
   			
   			content = content.replaceAll("public class "+appNames[0], "public class "+appNames[0]+ " extends SpringBootServletInitializer");
   			Files.write(path, content.getBytes(charset));

	}
	
	
	public void updatePomFile(String filePath) throws IOException {
		    Path path = Paths.get(filePath);
			Charset charset = StandardCharsets.UTF_8;

			String pomContent = new String(Files.readAllBytes(path), charset);
			int index = pomContent.indexOf("<dependency>");
			StringBuffer sb = new StringBuffer(pomContent);
			
			String fileContent = "";
			ClassPathResource cpr = new ClassPathResource("dependency.txt");
			byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
			fileContent = new String(bdata, StandardCharsets.UTF_8);
			sb.insert(index, fileContent+"\r\n");
			
			String PluginfileContent = "";
			ClassPathResource pCpr = new ClassPathResource("plugin.txt");
			byte[] Pbytes = FileCopyUtils.copyToByteArray(pCpr.getInputStream());
			PluginfileContent = new String(Pbytes, StandardCharsets.UTF_8);
			
			int pIndex = sb.indexOf("<plugin>");
			int eIndex = sb.lastIndexOf("</plugin>");
			sb.replace(pIndex, eIndex, PluginfileContent+"\r\n");
			//sb.insert(pIndex, PluginfileContent+"\r\n");
			
			
			Files.write(path, sb.toString().getBytes(charset));
	}
	
	public void executeMaven(String filePath) throws MavenInvocationException {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile( new File(filePath ) );
		request.setGoals( Collections.singletonList( "clean install" ) );

		Invoker invoker = new DefaultInvoker();
		invoker.setMavenHome(new File(mavenInstallationPath));

	     invoker.execute( request );
	}
	
	public String cloneRepository(String repository, String userName, String password) throws InvalidRemoteException, TransportException, GitAPIException {
		String[] repositoryNames = repository.split("/");
		String[] gitName = repositoryNames[repositoryNames.length - 1].split(".git");
		String appName = gitName[0];
		new File(pathToSave+File.separator+appName).mkdir();
		
		Git.cloneRepository()
		  .setURI(repository)
		  .setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, password))
		  .setDirectory(new File(pathToSave+File.separator+appName))
		  .call();
		
		return pathToSave+File.separator+appName;
	}
    
	
	public void deleteDir(String path) throws IOException {
		Path rootPath = Paths.get(path);
		try (Stream<Path> walk = Files.walk(rootPath)) {
		    walk.sorted(Comparator.reverseOrder())
		        .map(Path::toFile)
		        .peek(System.out::println)
		        .forEach(File::delete);
	}
	}
}
