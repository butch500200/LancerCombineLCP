package io.quarkus.lancer.combinelcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
@QuarkusMain
public class CombineLCP implements QuarkusApplication {

	public String newJsonDirectory = "./outputjson";
	public String outputZipTemp="./outputlcp";
	@Override
	public int run(String... args) throws IOException, InterruptedException {
		//Check if the proper args
		String inputDirectory = null;
		String outputFile = null;
		if (args.length < 2) {
			System.out.println("assuming default input arguments as lcp files are in this directory, and the output file will be modpack.lcp ");
			System.out.println("if this is not the case please press ctrl+c and run this from command line like ./combinelcp.exe <inputdirectory or ./ if there are in the same folder> <filename> ");
			inputDirectory = "./";
			outputFile = "modpack.lcp";
			Thread.sleep(5000);
		}else{
			inputDirectory = args[0];
			outputFile = args[1];
		}



		// Get all .lcp files from directory
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(inputDirectory), "*.lcp")) {
			// Object Mapper
			ObjectMapper mapper = new ObjectMapper();

			// Map to hold all JSON Nodes
			Map<String, ArrayNode> allJsonNodes = new HashMap<>();

			// Creating directories
			Path unzippedDirectoryPath = Paths.get(outputZipTemp);

			Path newJsonDirectoryPath = Paths.get(newJsonDirectory);
			Files.createDirectories( newJsonDirectoryPath);

			for (Path entry : stream) {
				// Unzip each .lcp file
				unzipFile(entry.toString(), outputZipTemp);
				//create temp directory for unzipped files
				Files.createDirectories(unzippedDirectoryPath);
				try (DirectoryStream<Path> jsonStream = Files.newDirectoryStream(unzippedDirectoryPath, "*.json")) {
					for (Path jsonFile : jsonStream) {
						String filename = jsonFile.getFileName().toString();
						JsonNode root = mapper.readTree(new File(jsonFile.toString()));

						allJsonNodes.putIfAbsent(filename, mapper.createArrayNode());

						if (root.isObject()) {
							allJsonNodes.get(filename).add(root);
						} else if (root.isArray()) {
							for (JsonNode node : root) {
								allJsonNodes.get(filename).add(node);
							}
						}

					}
				}

				deleteDirectory(unzippedDirectoryPath);
			}

			// Write JSON arrays to new files
			for (Map.Entry<String, ArrayNode> entry : allJsonNodes.entrySet()) {
				mapper.writerWithDefaultPrettyPrinter()
				      .writeValue(new File(newJsonDirectory+"/" + entry.getKey()), entry.getValue());
			}
			createManifest(newJsonDirectory+"/lcp_manifest.json");
			zipDirectory(new File(newJsonDirectory), new File("./" + outputFile));
		} finally {
			deleteDirectory(Paths.get(newJsonDirectory));
			deleteDirectory(Paths.get(outputZipTemp));
		}
		Path outputFilePath = Paths.get("./" + outputFile);
		if (Files.exists(outputFilePath)){
			System.exit(0);
			return 0;
		}else{
			System.exit(1);
			return 1;
		}


	}

	private void unzipFile(String zipFilePath, String destDirectory) throws IOException {
		ZipFile zipFile = new ZipFile(zipFilePath);
		Enumeration<?> enu = zipFile.entries();
		while (enu.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) enu.nextElement();
			Path path = Paths.get(destDirectory, zipEntry.getName());
			Files.createDirectories(path.getParent());
			Files.copy(zipFile.getInputStream(zipEntry), path, StandardCopyOption.REPLACE_EXISTING);
		}
		zipFile.close();
	}

	private void createManifest(String filepath) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode manifestObject = mapper.createObjectNode();
		manifestObject.put("name", "Lancer Mod Pack");
		manifestObject.put("author", "Joseph Cassidy");
		manifestObject.put("version", "1.0");
		manifestObject.put("description", "combining all .lcp files into one mega one. This helps when we need to reload .lcp files");
		manifestObject.put("item_prefix", "modpack");


		mapper.writerWithDefaultPrettyPrinter()
		      .writeValue(new File(filepath), manifestObject);
	}


	private void zipDirectory(File directoryToZip, File zipFile) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(zipFile);
		     ZipOutputStream zos = new ZipOutputStream(fos)) {

			File[] files = directoryToZip.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						// Add a new ZipEntry
						ZipEntry zipEntry = new ZipEntry(file.getName());
						zos.putNextEntry(zipEntry);

						// Write the file content
						Files.copy(file.toPath(), zos);

						// Close the current entry
						zos.closeEntry();
					}
				}
			}

		}
	}

	private void deleteDirectory(Path directoryPath) throws IOException {
		if (Files.exists(directoryPath)) {
			Files.walk(directoryPath)
			     .map(Path::toFile)
			     .sorted((o1, o2) -> -o1.compareTo(o2))
			     .forEach(File::delete);
		}
	}
}
