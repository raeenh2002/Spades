# README #

This README tells whatever steps are necessary to get our application up and running.

### What is this repository for? ###

* This is the project for the visualization of user watch and phone accelerometer data.
* 1.0
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### How do I get set up? ###

* **Summary of set up**: You just need the war file along with the categorized data files. Extract the war file to your server. Also copy the data (data and survey folders) to the server. Now when you run the URL to the location you extracted the war file, the chart will be generated (after some time depending on your data).

* **Detailed setup**: Using an application server is necessary ([Tomcat](http://tomcat.apache.org/) is recommended)

1. Put the war file in the **webapps** folder of where your application server (Tomcat) is installed.

2. Run Tomcat (start the server) - It will automatically extract the war file in that folder.

   * If not, manually extract the war file contents to that folder (using winrar or winzip)

3. Copy the **data** and **survey** folder containing your data to root folder of which your application server (Tomcat) is installed. The data folder contains folders of each year and inside are it's months and so on. The files in these folders are the accelerometer data, battery data for both watch and phone along with annotation labels. The survey folder contains folders with exact dates for prompts.

4. Go to the URL of which the war file is located using a browser (for Tomcat it will be **localhost:8080/Spades**). After a few minutes (depending on the size of your data), the chart will be drawn.

5. For enabling and disabling labels and prompts simply use **Y** or **N** following that URL (localhost:8080/Spades/index.jsp?label=Y&prompt=Y). Y is for Yes and N is for No.

6. For printing, you can use the free **[Full Page Screen Capture](https://chrome.google.com/webstore/detail/full-page-screen-capture/fdpohaocaechififmbbbbbknoalclacl?hl=en-US)** extension for chrome. It gives you an image of the whole chart.

PS: There are lots of other extensions and programs to capture the whole page as an image (i.e **[Open Screenshot](https://chrome.google.com/webstore/detail/open-screenshot/akgpcdalpfphjmfifkmfbpdmgdmeeaeo?hl=en)** for chrome).