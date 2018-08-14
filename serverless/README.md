 OpenWhisk, currently in incubation at Apache, is an open-source implementation of FaaS that lets you 
 create functions that are invoked in response to events. 

To run the application, follow these steps:

1. Notice: the zip file. This contains __main__.py,dscovr_file_name_parser.py and dscovrIsoLiteTemplate.xml.
2. From the OpenWhisk CLI command line, create the action by entering this command:

    ```
    wsk -i action create --kind python dscovrScript dscovrMetadataGenerationResources.zip
    ```
3. Then, invoke the action by entering the following commands.
    ```
    wsk -i action invoke dscovrScript -p message test
    ```
   This command outputs:
    ```
    should return a blob of xml
     ```

    If the script did not work, run this to view the logs-
    ````
    wsk -i activation poll
    ````
    