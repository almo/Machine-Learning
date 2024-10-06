#
# Probabilistic Learning - Classification Using Naive Bayes
#
# Resources
# https://github.com/PacktPublishing/Machine-Learning-with-R-Fourth-Edition
# chapter four
#
# References:
#  - On the Optimality of the Simple Bayesian Classifier under Zero-One Loss
#    https://link.springer.com/article/10.1023/A:1007413511361
#
# The strengths and weaknesses of this algorithm are as follows
# Strengths:
#  - Simple, fast, and very effective
#  - Does well with noisy and missing data and large number of features
#  - Requires relatively few examples for training
#  - Easy to obtain the estimate probability for a prediction
# Weaknesses
#  - Relies on an often-faulty assumption of equally important and independent
#    features
#  - Not ideal for dataset with many numeric features
#  - Estimated probabilities are less reliable than the predicted classes.
#  Notes:
#  Laplace Estimator: The Laplace Estimator add a small number to each of the
#  counts in frequency table, which ensures that each feature has a non-zero
#  probability of occurring with each class.
#
#  Using numeric features with Naive Bayes: Naive Bayes uses frequency tables
#  for learning the data, which means that each feature must be categorical in
#  order to create the combination of class and feature values comprising the
#  matrix. Therefore the algorithm does not work with numeric data.
#  One easy and effective solution is to discretize numeric features, which
#  simply means that the numbers are put into categories known as bin (binning).
#  The method works best when there are large amounts of training data.
#  Important: discretizing a numeric feature always results in a reduction of
#  information, as the feature's original granularity is reduce to a smaller
#  number of categories.

# Data set is the SMS Spam Collection
# Ref: On the Validity of a New SMS Spam Collection
#      https://ieeexplore.ieee.org/document/6406757
sms_raw <- read.csv("sms_spam.csv")
str(sms_raw)

# Converting type in a factor
sms_raw$type <- factor(sms_raw$type)
str(sms_raw$type)
table(sms_raw$type)

# Data Preparation - cleaning and standardizing text data
# Installing the text mining package
install.packages("tm")
library("tm")
help(package="tm")


# Text Mining Package allows two type of corpus VCorpus (volitile, in memory) or
# PCorpus (permanent, in file system, databases, etc...)
# The parameter readerControl can be used by creating corpus from PDF or
# MS Office files.
sms_corpus <- VCorpus(VectorSource(sms_raw$text))
print(sms_corpus)
inspect(sms_corpus[1:3])
# Printing entries one by one
as.character(sms_corpus[1])
as.character(sms_corpus[2])
as.character(sms_corpus[3])
# Printing several entries
lapply(sms_corpus[4:6],as.character)

# Lowering characters using tm_map
sms_corpus_clean <- tm_map(sms_corpus, content_transformer(tolower))
as.character(sms_corpus[5])
as.character(sms_corpus_clean[5])

# Removing numbers
sms_corpus_clean <- tm_map(sms_corpus_clean, removeNumbers)
as.character(sms_corpus[5])
as.character(sms_corpus_clean[5])

# Removing stop words (and, but, or, etc....)
sms_corpus_clean <- tm_map(sms_corpus_clean, removeWords, stopwords())
as.character(sms_corpus[5])
as.character(sms_corpus_clean[5])

# Removing punctuation
sms_corpus_clean <- tm_map(sms_corpus_clean, removePunctuation)
as.character(sms_corpus[5])
as.character(sms_corpus_clean[5])

# Alternatively, replacing punctiation w/ blanks might be better
replacePunctiation <- function(x) {
  gsub("[[:punct:]]+"," ", x)
}
replacePunctiation("Hello...World")

# Steaming i.e. replacing words like learned, leans or learning w/ learn
# Using SnowballC library http://snowballstem.org
install.packages("SnowballC")
library("SnowballC")
wordStem(c("learn","learning","learned","learns"))

sms_corpus_clean <- tm_map(sms_corpus_clean, stemDocument)
as.character(sms_corpus[1:3])
as.character(sms_corpus_clean[1:3])

# Tokenization i.e splitting text documents into words
# Text Mining's function DocumentTermMatrix creates a data structure called a
# document-term matrix. The package also provide the TermDocumentMatrix, which
# is the transposed of document-term matrix. Depending if the number of
# documents is high might be interesting using one or another.
sms_dtm <- DocumentTermMatrix(sms_corpus_clean)
sms_dtm

# Alternatively, all preparation can be done in just one call
sms_dtm2 <- DocumentTermMatrix(sms_corpus, control = list(
  tolower = TRUE,
  removeNumbers = TRUE,
  stopwords = TRUE,
  removePunctuation = TRUE,
  stemming = TRUE
))
sms_dtm2

# Crearting training and test datasets
sms_dtm_train <- sms_dtm[1:4169,]
sms_dtm_test <- sms_dtm[4170:5559,]

# Preparing labels
sms_train_labels <- sms_raw[1:4169,]$type
sms_test_labels <- sms_raw[4170:5559,]$type

prop.table(table(sms_train_labels))
prop.table(table(sms_test_labels))

# Visualizing text data
install.packages("wordcloud")
library("wordcloud")
wordcloud(sms_corpus_clean, min.freq=50, random.order=FALSE)

spam <- subset(sms_raw, type=="spam")
ham <- subset(sms_raw, type=="ham")
wordcloud(spam$text, max.words = 40, scale = c(3,0.5))
wordcloud(ham$text, max.words = 40, scale = c(3,0.5))


# Crearting indicator features for frequent words
# Selecting features i.e. words apearing in at least 5 SMS
sms_freq_words <- findFreqTerms(sms_dtm_train, 5)
str(sms_freq_words)

sms_dtm_freq_train <- sms_dtm_train[,sms_freq_words]
sms_dtm_freq_test <- sms_dtm_test[,sms_freq_words]

## Converting numeric features into categorical
convert_counts <- function (x){
  x <- ifelse(x>0, "Yes", "No")
}
sms_train <- apply(sms_dtm_freq_train, MARGIN = 2, convert_counts)
sms_test <- apply(sms_dtm_freq_test, MARGIN = 2, convert_counts)

## Training the model
install.packages("naivebayes")
library(naivebayes)
sms_classifier <- naive_bayes(sms_train,sms_train_labels)
# Warnings are because zero probabilies are present. Laplace smoothing is
# required.
warnings()

# Evaluating model performance
sms_test_pred <- predict(sms_classifier, sms_test)
library(gmodels)
CrossTable(sms_test_pred, sms_test_labels, prop.chisq = FALSE, prop.c= FALSE,
           prop.r = FALSE, dnn = c('predicted','actual') )

# Improving model with Laplace Smoothing
sms_classifier2 <- naive_bayes(sms_train, sms_train_labels, laplace = 1)
sms_test_pred2 <- predict (sms_classifier2, sms_test)
CrossTable(sms_test_pred2, sms_test_labels, prop.chisq = FALSE, prop.c= FALSE,
           prop.r = FALSE, dnn = c('predicted','actual') )
