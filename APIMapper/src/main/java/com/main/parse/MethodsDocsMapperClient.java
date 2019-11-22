package com.main.parse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;

import com.database.mysql.LibraryDocumentationDB;
import com.database.mysql.MigrationMappingDB;
import com.database.mysql.MigrationRule;
import com.database.mysql.MigrationRuleDB;
import com.database.mysql.MigrationSegmentsDB;
import com.database.mysql.RepositoriesDB;
import com.library.Docs.MethodDocs;
import com.library.source.MethodObj;
import com.library.source.MigratedLibraries;
import com.segments.build.Segment;
import com.segments.build.TerminalCommand;
import com.subversions.process.GitRepositoryManager;

public class MethodsDocsMapperClient {
	static String projectPath = Paths.get(".").toAbsolutePath().normalize().toString();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new MethodsDocsMapperClient().run();
	}

	TerminalCommand terminalCommand = new TerminalCommand();
	String outputHMTLFile = "APIMapperOutput.html";

	void run() {

		RepositoriesDB repositoriesDB = new RepositoriesDB();

		LinkedList<MigrationRule> migrationRules = new MigrationRuleDB().getMigrationRulesWithoutVersion(1);
		// String htmlData="<div class=\"container\">\n";

		String htmlData = "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<title>APIMapper output</title>\n<meta charset=\"utf-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/css/bootstrap.min.css\">\n</head>\n<body style=\"background-color: #eee\">\n";

		htmlData += "<div class=\"container\" style=\"background-color: #FFFFFF;padding: 20px\">\n</br>\n<div class=\"alert alert-success\">\n";
		htmlData += "<h3 style=\"text-align: center;\"> List Of Detected  Library Migrations, Code fragments, method documentation that generated by <strong><a href=\"https://github.com/hussien89aa/APIMapper\">APIMapper</a></strong>.</h2>\n</div> \n</br>";

		for (MigrationRule migrationRule : migrationRules) {
			// System.out.println("==> Start search for migration rule "+
			// migrationRule.FromLibrary + "<==> "+ migrationRule.ToLibrary);

			// htmlData+="<div class=\"alert alert-success\">Start search for migration rule
			// "+ migrationRule.FromLibrary + "<==> "+ migrationRule.ToLibrary +"</div>\n";
			htmlData += " \n<div class=\"panel panel-primary\">\n<div class=\"panel-heading\">\n Migration Rule "
					+ migrationRule.FromLibrary + " <==> " + migrationRule.ToLibrary
					+ "\n</div>\n<div class=\"panel-body\">\n";

			// Load code fragments for migration Rule
			ArrayList<Segment> segmentList = new MigrationMappingDB().getFunctionMapping(String.valueOf(migrationRule.ID), false, false);

			// <div class="row">
			// <div class="col-sm-6" style="background-color:#ef2846;">.col-sm-4</div>
			// <div class="col-sm-6" style="background-color:#42f453;">.col-sm-4</div>
			// <div class="col-sm-12" style="background-color:#278eef;">
			// +A:<br/>

			// </div>
			// Map Docs to function signature

			for (Segment segment : segmentList) {
				//String appLink = repositoriesDB.getRepositoriesLink(segment.AppID);
				String removeMethods = "";
				String docsRemoveMethods = "";
				String addMethods = "";
				String docsAddMethods = "";
				ArrayList<String> listOfCommits = new MigrationMappingDB().getCommitsWithMapping(migrationRule.ID, segment.MigrationMappingID);

				for (String methodSignature : segment.removedCode) {

					MethodObj methodFormObj = MethodObj.GenerateSignature(methodSignature);
					// Load Library Docs for migration Rule, We load all versions docs in case the
					// migration version dosenot have we get it from other
					ArrayList<MethodDocs> fromLibrary = new LibraryDocumentationDB().getDocs(migrationRule.FromLibrary,
							methodFormObj.methodName);

					MethodDocs methodFromDocs = MethodDocs.GetObjDocs(fromLibrary, methodFormObj);

					// Docs to HTML
					removeMethods += "- " + methodSignature + "<br/>\n";
					if (methodFromDocs.fullName.length() > 0) {
						docsRemoveMethods += "<p style=\"color:#ef2846;\">\n";
						docsRemoveMethods += "- " + methodFromDocs.fullName + "<br/>\n";
						docsRemoveMethods += "<br/><strong>Description:</strong><br/>\n"
								+ methodFromDocs.description.replaceAll("<", "&lt;").replaceAll(">", "&gt;")
								+ "<br/>\n";
						// docsRemoveMethods+= methodFromDocs.inputParams+"<br/>\n"+
						// methodFromDocs.returnParams +"<br/>\n";
						if (methodFromDocs.inputParams.length() > 0) {
							docsRemoveMethods += "<br/><strong>Parameters:</strong>\n<ul style=\"color:#ef2846;\">\n";
							for (String param : methodFromDocs.inputParams.split("\\|\\|")) {
								docsRemoveMethods += "\t<li style=\"color:#ef2846;\">" + param + "</li>\n";
							}
							docsRemoveMethods += "</ul>\n";
							// docsRemoveMethods+= methodFromDocs.inputParams+"<br/>\n";
						}
						if (methodFromDocs.returnParams.length() > 0) {
							docsRemoveMethods += "<br/><strong>Return Parameters:</strong>\n<ul style=\"color:#ef2846;\" >\n";
							for (String param : methodFromDocs.returnParams.split("\\|\\|")) {
								docsRemoveMethods += "\t<li style=\"color:#ef2846;\">" + param + "</li>\n";
							}
							docsRemoveMethods += "</ul>\n";
							// docsRemoveMethods+= methodFromDocs.returnParams +"<br/>\n";
						}
						docsRemoveMethods += "</p><hr/>\n";
					}

					// System.out.println("> "+ methodSignature);
					// System.out.println(methodFromDocs.description);
				}

				// System.out.println("-------");
				for (String methodSignature : segment.addedCode) {

					MethodObj methodFormObj = MethodObj.GenerateSignature(methodSignature);
					ArrayList<MethodDocs> toLibrary = new LibraryDocumentationDB().getDocs(migrationRule.ToLibrary,
							methodFormObj.methodName);

					MethodDocs methodFromDocs = MethodDocs.GetObjDocs(toLibrary, methodFormObj);

					// Docs to HTML
					addMethods += "+ " + methodSignature + "<br/>\n";
					if (methodFromDocs.fullName.length() > 0) {
						docsAddMethods += "<p style=\"color:#009933;\">\n";
						docsAddMethods += "+ " + methodFromDocs.fullName + "<br/>\n";
						docsAddMethods += "<br/><strong>Description:</strong><br/>\n"
								+ methodFromDocs.description.replaceAll("<", "&lt;").replaceAll(">", "&gt;")
								+ "<br/>\n";
						if (methodFromDocs.inputParams.length() > 0) {
							docsAddMethods += "<br/><strong>Parameters:</strong>\n<ul style=\"color:#009933;\" >\n";
							for (String param : methodFromDocs.inputParams.split("\\|\\|")) {
								docsAddMethods += "\t<li style=\"color:#009933;\" >" + param + "</li>\n";
							}
							docsAddMethods += "</ul>\n";
							// docsAddMethods+= methodFromDocs.inputParams+"<br/>\n";
						}
						if (methodFromDocs.returnParams.length() > 0) {
							// docsAddMethods+= methodFromDocs.returnParams +"<br/>\n";
							docsAddMethods += "<br/><strong>Return Parameters:</strong>\n<ul style=\"color:#009933;\" >\n";
							for (String param : methodFromDocs.returnParams.split("\\|\\|")) {
								docsAddMethods += "\t<li style=\"color:#009933;\" >" + param + "</li>\n";
							}
							docsAddMethods += "</ul>\n";
						}
						// docsAddMethods+= methodFromDocs.inputParams+"<br/>\n"+
						// methodFromDocs.returnParams +"<br/>\n";
						docsAddMethods += "</p><hr/>\n";
					}

				}

				htmlData += " \n<div class=\"panel panel-default\">\n<div class=\"panel-heading\"> Method Mapping\n</div>\n<div class=\"panel-body\">\n";
				htmlData += "<div class=\"row\">\n";
				htmlData += "<div class=\"col-sm-6\" >\n<code style=\"color:#ef2846;\">" + removeMethods
						+ "</code>\n</div>\n";
				htmlData += "<div class=\"col-sm-6\" >\n<code style=\"color:#009933;\">" + addMethods
						+ "</code>\n</div>\n";

				if (docsRemoveMethods.trim().length() != 0 || docsAddMethods.trim().length() != 0) {
					htmlData += "</br></br></br>";
					htmlData += "</br>\n</br>\n<div class=\"col-sm-12\">\n";
					htmlData += " \n<div class=\"panel panel-info\">\n<div class=\"panel-heading\">\n Documentation \n</div>\n<div class=\"panel-body\">\n";
					htmlData += docsRemoveMethods + docsAddMethods + "\n</div>\n</div>\n";
					htmlData += "\n</div>\n";
				}
				
				htmlData += "<br/> - List Of Real code commits has this method mapping:<ul >\n";  
				for(String commitLink: listOfCommits) {
					htmlData += "<li ><a href=\""+ commitLink + "\">"+commitLink+"</a></li>\n";  
				}
				htmlData += "</ul>\n";  
				
				htmlData += "</div>\n"; // For row
				htmlData += "\n</div>\n</div>\n"; // for panel
			}

			// End migration rule panel
			htmlData += "\n</div>\n</div>\n";
		}
		htmlData += "</div>\n";
		htmlData += "</body>\n</html>";

		// write to file
		terminalCommand.deleteFolder(outputHMTLFile);
		// GitRepositoryManager gitRepositoryManager= new GitRepositoryManager();
		// gitRepositoryManager.saveCleanLinks(htmlData, outputHMTLFile);
		try {

			Path path = Paths.get(outputHMTLFile);

			Files.write(path, htmlData.getBytes());
			System.out.println("OutputFile is created under " + path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println(htmlData);

	}

}
