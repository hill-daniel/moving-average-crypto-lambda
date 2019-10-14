# floating-average-crypto
## Moving average calculation for crypto currency.

Written in 2017

This is mainly a project to try out different technologies with a (more or less) real use case.

### What is it for?
This code should be deployed as a lambda function in AWS.
During execution it will pull crypto currencies US Dollar values from an API and store them as ProtoBuf format in S3.
Currently it is designed to calculate the 200 days moving average of each currency, hence it should run once a day.
So, yeah you have to wait 200 days before you actually have the average if you start from scratch...
It will notify any subscribers to an AWS SNS topic if the current value of the currency deviates more or less than a percentage threshold.

### how to use
* have an AWS Account
* create an S3 bucket
* upload currency.json to this bucket and configure individual currencies if needed
* provide S3 bucket in the config.properties file as _aws.s3.bucket.name_
* mvn clean package
* create a Java 8 Lambda function, provide a SNS topic as Environment Variable _floatingAverageWarningTopic_
* upload the fat jar
* run for test
* configure AWS CloudWatch to run Lambda function once a day

Technology used in this one:
* [Java 8](https://java.com/de/) Programming language
* [Google Guice](https://github.com/google/guice) Dependency injection
* [Google AutoValue](https://github.com/google/auto/tree/master/value) Immutable value types for Java 
* [Google Protocol Buffers](https://developers.google.com/protocol-buffers/) Structured data serialization
* [AWS Lambda](https://aws.amazon.com/lambda/) Serverless code execution
* [AWS S3](https://aws.amazon.com/s3/) Simple (file) storage
* [AWS SNS](https://aws.amazon.com/sns/) Notifications (E-Mail, Push, SMS...)
* [AWS CloudWatch](https://aws.amazon.com/cloudwatch/) Events, Logs, Metrics
* [UniRest](http://unirest.io/) REST client
