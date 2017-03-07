# Twitalyzr

**Twitalyzr** (Twitter stream analyzer) is small application which is working on input twitter stream with particular hashtag (in our case #Cassandra) and it is processing each tweet marking it as Apache Cassandra related or not Apache Cassandra related. Decision whether it is Cassandra related or not is done based on tweet content and description of person posting some tweet. Reason to make this application is because a lot of tweets with #cassandra are not relevant to database. Due it's name, there are tweets about porn stars, soap operas, book characters, game characters, various sales, etc.

Application contains two Spark jobs:
  1. Training Spark job: Make and optimize preprocessing and training pipeline
  2. Streaming Spark job: Use of model on stream to classify upcoming tweets in real time

Because there is predefined [model](model), there is not need for training (unless if you want to play). This pretrained model uses Logistic Regression with preprocessing steps: clean, tokenize, stop word remover, n-gram, term frequency. Preprocessing steps are done on text of tweet, text of user description and hashtags. Model is trained with k-fold cross validation, and on test set it achieved 0.99 accuracy.

## Start streaming
If you want to run stream job on AWS we already provide automatization via ansible in folder [automation](automation).   
  1. Compile project with *sbt*:  
  ```
  sbt assembly
  ```      
  2. export *AWS_ACCESS_KEY_ID* and *AWS_SECRET_ACCESS_KEY* for AWS. In [spark.yml](automation/spark.yml) there is additional AWS configuration, like type of instance, etc.  
  3. Twitter API keys are encrypted with ansible-vault and they are in [twitter_api_keys.yml](automation/roles/twitalyzr_deploy/vars). Twitter API keys are used to fill template with:  
   
   ```yml
      token = "{{ token }}"  
      tokenSecret = "{{ token_secret }}"  
      consumerKey = "{{ consumer_key }}"  
      consumerSecret = "{{ consumer_secret }}"  
    ```  
  4. Export variables that represent path for jar and model:  
  
    ```
    export TWITALYZR_SRC_JAR=<path_to_directory_that_contains_jar>  
    export TWITALYZR_SRC_MODEL=<path_to_tar_containing_model>  
    ```  
  5. Execute next line (from [automation](automation) dir) if instance is already created, if not just change tag to ```aws-setup```:  
  ```
  ansible-playbook -vv -i inventory/ --private-key <path_to_pem> spark.yml --tags aws-start --ask-vault-pass
  ```  
