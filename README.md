# twicas
Detection of valuable and non-valuable tweats based on Cassandra hash tag.

Valuable tweets are ones about cassandra database, and non-valuable are rest (sex, soap operas...). This example includes prediction on tweets that are streamed from twitter api.

Twicas uses Spark machine learning library and Spark streaming

## Twicas model
This example has 2 ipython notebooks.
* [logreg_model.ipynb](https://github.com/smartcat-labs/twicas/blob/master/logreg_model.ipynb) is about making linear regression model with labeled dataset
* [twitter_streaming.ipynb](https://github.com/smartcat-labs/twicas/blob/master/twitter_streaming.ipynb) is about using Spark streaming and Twitter stream api for real time prediction if its tweet #cassandra valuable or not.

## Train set
For train, model uses datasets of labeled tweets from different hashtags. Hashtags that are used for building train set are:
* #cassandra
* #spark
* #bigdata
* #porn
* #softwareengineer
* #datastax

All those sets are parsed and saved in json files.

## Logistic regression model
First of all, json files are read into Spark DataFrames, where every row contains data from one tweet. For training puprose only text of tweet and label are used. Set is splited in training set and test set.
Next step was features extraction. That is cleaning text of tweets and tokeniezed using RegexTokenization from Spark. After that, tokens are used into HashingTF to make features.
For example, suppose we have tweet like "This_is my    very #frist #tweet". And suppose we want to extract in into 4 feature.

1. Cleaning: "this is my very first tweet"

2. Tokenization: [this, is, my, very, first, tweet]

3. HashingTF: SparseVector(4,[0,2,3],[2,3,1])

4. Labeling: LabelPoint(label=0,features=SparseVector(4,[0,2,3],[2,3,1]))

RDD containing labelpoints are applied to learning model: LogisticRegressionWithLBFGS

Model is tested on test set and saved to file

##Twitter Streaming
Using Spark streaming and Twitter stream api, previously built model is used to predict new tweets.
