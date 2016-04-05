# README #

This README tells whatever steps are necessary to get our application up and running.

### What is this repository for? ###

* This is the project for the visualization of user watch and phone accelerometer data.
* 1.0
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### How do I get set up? ###

* **Summary of set up**: You just need the war file along with the categorized data files. Extract the war file to your server. Also copy the data (data and survey folders) to the server. Now when you run the URL to the location you extracted the war file, the chart will be generated (after some time depending on your data).

* **Detailed setup**: Using an application server is necessary ([**Tomcat**](http://tomcat.apache.org/) is recommended: download the latest version of Tomcat. It will download and exe file from the link, then follow the instructions to install the program)

1) Put the war file from this repository in the **webapps** folder of where your application server (Tomcat) is installed.

   * Tomcat is usually installed in "C:\Program Files\Apache Software Foundation\Tomcat #.#" and the **webapps** folder is in that location.

2) Run Tomcat (start the server) by opening **Monitor Tomcat** from your installed programs. An icon will appear in the bottom right tray area. Right click on the icon and select **start service**. Wait for it to finish.

   * After that it will automatically extract the war file in that folder. (You don't need to do anything)

   * If by some chance it didn't, manually extract the war file contents to the **webapps** folder (using winrar or winzip)

      * You can check the folder for a new folder named **Spades**

3) Copy the **data** and **survey** folder containing your data to the root folder of which your application server (Tomcat) is installed ("C:\Program Files\Apache Software Foundation\Tomcat #.#").

      * The data folder contains folders of each year and inside are it's months and so on ("2015/01/...").

           * The files in these folders are the accelerometer data, battery data for both watch and phone along with annotation labels.

      * The survey folder contains folders with exact dates for prompts ("2015-10-01/...").

4) After that using a browser, go to the URL of which the war file is located (for Tomcat it will be **localhost:8080/Spades**). After a few minutes (depending on the size of your data), the chart will be drawn. Usually 15mins for 3 months of data.

5) For enabling and disabling labels and prompts simply use **Y** or **N** following that URL (localhost:8080/Spades/index.jsp?label=Y&prompt=Y). Y is for Yes and N is for No.

6) For printing, you can use the free **[Full Page Screen Capture](https://chrome.google.com/webstore/detail/full-page-screen-capture/fdpohaocaechififmbbbbbknoalclacl?hl=en-US)** extension for chrome. It gives you an image of the whole chart.

PS: There are lots of other extensions and programs to capture the whole page as an image (i.e **[Open Screenshot](https://chrome.google.com/webstore/detail/open-screenshot/akgpcdalpfphjmfifkmfbpdmgdmeeaeo?hl=en)** for chrome and so on).