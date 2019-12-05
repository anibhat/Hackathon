package com.example.hackathon.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

@Service
public class ConverterUtil {
	
	@Value("${pathToClone}")
	public String pathToSave;
	
	@Value("${mavenInstallationPath}")
	public String mavenInstallationPath;
	
	public  List<String> fileList = new ArrayList<String>();
	
	public Map<String, String> projectDescription = new HashMap<String, String>();
	
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

	public  List<String> getFileList() {
		return fileList;
	}

	public  void setFileList(List<String> fileList) {
		fileList = fileList;
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
			projectDescription.put("appName", appNames[0]);
			String content = new String(Files.readAllBytes(path), charset);
   			if (content.startsWith("package")) {
   				int index = content.indexOf(";");
   				String sub = content.substring(0, index+1);
   				String[] packageNames = sub.split("package ");
   				String packageName = packageNames[1];
   				StringBuffer sb = new StringBuffer(content);
   				sb.insert(index+2, "\r\n"+"import org.springframework.boot.web.support.SpringBootServletInitializer;");
   				content = sb.toString();
   				
   				projectDescription.put("packageName", packageName);
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
			projectDescription.put("pompath", filePath);
			
			String pomContent = new String(Files.readAllBytes(path), charset);
			
			int descIndex = pomContent.indexOf("<description>");
			int descEndIndex = pomContent.indexOf("</description>");
			
			String descriprion = pomContent.substring(descIndex, descEndIndex).split("<description>")[1];
					;
			projectDescription.put("description", descriprion);
			
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
			
			int pIndex = sb.indexOf("<build>");
			int eIndex = sb.lastIndexOf("</build>");
			sb.delete(pIndex, eIndex);
			String val = sb.toString();
			val = val.replace("</build>", PluginfileContent+"\r\n");
			//sb.insert(pIndex, PluginfileContent+"\r\n");
			
			
			Files.write(path, val.getBytes(charset));
	}
	
	public void executeMaven(String filePath) throws MavenInvocationException {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile( new File(filePath ) );
		request.setGoals( Collections.singletonList( "clean package" ) );

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
			walk.sorted(Comparator.reverseOrder()).map(Path::toFile).peek(System.out::println).forEach(File::delete);
		}
	}
	
	public void updateSam() throws IOException {
		
		String pomPath = projectDescription.get("pompath");
		String uri= Paths.get(pomPath).getParent().toString();
		File samFile = new File(uri+File.separator+"sam.yaml");
		samFile.createNewFile();
		Path samPath = Paths.get(uri+File.separator+"sam.yaml");
		
		projectDescription.put("rootPath", uri);
		
		String samContent = "";
		ClassPathResource pCpr = new ClassPathResource("sam.txt");
		byte[] Pbytes = FileCopyUtils.copyToByteArray(pCpr.getInputStream());
		samContent = new String(Pbytes, StandardCharsets.UTF_8);
		
		samContent = samContent.replace("Description: New", "Description: "+projectDescription.get("description"));
		samContent = samContent.replace("com.amazonaws.serverless.sample.springboot.",projectDescription.get("packageName").replace(";", "."));
		samContent = samContent.replace("SpringBoot123456", projectDescription.get("appName"));
		//samContent = samContent.replace("PetStoreFunction", "ServerlessApiFunction");
		Files.write(samPath, samContent.getBytes("UTF-8"));
	}
	
	
	public void updateBin() throws IOException {
		String binPath = projectDescription.get("rootPath");
		String binContent = "";
		ClassPathResource pCpr = new ClassPathResource("bin.txt");
		byte[] Pbytes = FileCopyUtils.copyToByteArray(pCpr.getInputStream());
		binContent = new String(Pbytes, StandardCharsets.UTF_8);
		
		File binFile = new File(binPath+File.separator+"bin.xml");
		binFile.createNewFile();
		Path path = Paths.get(binPath+File.separator+"bin.xml");
		
		Files.write(path, binContent.getBytes("UTF-8"));
	}
	
	public void cloudFormation() {
		 ProcessBuilder processBuilder = new ProcessBuilder();
	        // Windows
	        //processBuilder.command("bash", "-c", "aws cloudformation package --template-file "+projectDescription.get("rootPath")+File.separator+"sam.yaml --output-template-file output-sam.yaml --s3-bucket Test ; aws cloudformation deploy --template-file output-sam.yaml --stack-name "+ projectDescription.get("appName")+" --capabilities CAPABILITY_IAM ; aws cloudformation describe-stacks --stack-name "+ projectDescription.get("appName"));
	        
	        StringBuffer sb = new StringBuffer();
	        sb.append("aws cloudformation package --template-file ");
	        sb.append(projectDescription.get("rootPath")+File.separator);
	        sb.append("sam.yaml --output-template-file ");
	        sb.append(projectDescription.get("rootPath")+File.separator);
	        sb.append("output-sam.yaml --s3-bucket aws-serverless-springboot-app-001 ;");
	        sb.append("aws cloudformation deploy --template-file ");
	        sb.append(projectDescription.get("rootPath")+File.separator);
	        sb.append("output-sam.yaml --stack-name ServerlessSpringBootApp --capabilities CAPABILITY_IAM ;");
	        sb.append(" aws cloudformation describe-stacks --stack-name ServerlessSpringBootApp");
	        System.out.println(sb.toString());
	        processBuilder.command("bash", "-c", sb.toString());
	        //processBuilder.command("/home/ec2-user/migrator/aws/aws-serverless-java-container/samples/springboot/pet-store/deploy.sh");

	        try {

	            Process process = processBuilder.start();

	            BufferedReader reader =
	                    new BufferedReader(new InputStreamReader(process.getInputStream()));

	            String line;
	            while ((line = reader.readLine()) != null) {
	                System.out.println(line);
	            }

	            int exitCode = process.waitFor();
	            System.out.println("\nExited with error code : " + exitCode);

	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
		
	}
}
