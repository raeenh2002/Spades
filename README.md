# README #

This README tells whatever steps are necessary to get our application up and running.

### What is this repository for? ###

* This is the project for the visualization of user watch and phone accelerometer data.
* 1.0
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### How do I get set up? ###

* Summary of set up: You just need the war file along with your categorized data files. Extract the war file to the location of your server. Also copy the data (data and survey folders) to the location of the server.
* Configuration: When the war contents and the data are correctly placed, just go to the url of where the file is located (For Tomcat it would be localhost:8080/Spades). If you want to have the option of hiding labels and/or prompts in the chart, simply pass the parameter "label" and "prompt" in the URL (localhost:8080/Spades/index.jsp?label=n&prompt=n). If you don't want to show either of them, simply change the "n"s in the url to "y".
* Database configuration: Put the "data" and "survey" folders in the server's working directory.