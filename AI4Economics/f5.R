#
# Divide and Conquer - Classification Using Decision Trees and Rules
#
# Resources
# https://github.com/PacktPublishing/Machine-Learning-with-R-Fourth-Edition
# chapter five
#
# C5.0 Algorithm
# Strengths
#  - An all-purpose classifier that does well on many types of problems
#  - Highly automatic learning process, which can handles numerical or nominal
#    features, as well as missing data.
#  - Excludes unimportant features
#  - Can be used on both small and large datasets
#  - Result in a model that can be interpreted without a mathematical background
#    (for relatively small trees)
#  - More efficient than other complex models
# Weaknesses
#  - Decision trees models are often biased towards splits on features having a
#    large number of levels
#  - It is easy to overfit or underfit the model
#  - Can have trouble modeling some relationships due to reliance on
#    axis-parallel splits
#  - Small changes in training data can result in large changes to decision
#    logic
#  - Large trees can be difficult to interpret and the decisions they make may
#    seem counterintuitive

# Information gain determines the change of homogeneity (Entropy) resulting from
# a split on each possible feature. This is not the only splitting criteria.
# Other common criteria are the Gini Index, chi-squared statistic and gain ratio.
# Interesting paper comparing splitting methods:
# An Empirical Comparison of Selection Measures for Decision-Tree Induction
# Entropy example:
#  - Dataset: AAA, BBBB, CC, D
#  - SUM(- (3/10 * log2(3/10) - (4/10 * log2(4/10)) - (2/10 * log2(2/10))
#        - (1/10 * log2(1/10))
entropy <-(-3/10 * log2(3/10)) + (-4/10 * log2(4/10)) + (-2/10 * log2(2/10)) + (-1/10 * log2(1/10))
entropy # 1.846439

# Entropy visualization
curve(-(x *log2(x))-((1-x)*log2(1-x)),col="red", xlab="x", ylab="Entropy", lwd=5)


# The process of pruning a decision tree involves reducing its size such that
# it generalizes better to unseen data.
# Interesting paper about pruning:
# A Comparative Analysis of Methods for Pruning Decision Trees

# Identifying risky bank loans using C5.0
# Loading data
credit <- read.csv("credit.csv",stringsAsFactors = TRUE)

# Exploring data
str(credit)
table (credit$checking_balance)
table(credit$savings_balance)

summary(credit$months_loan_duration)
summary(credit$amount)

table(credit$default)

# Splitting dataset into training / test
# Establishing a seed (the text uses 9829) allows repeating the experiment
set.seed(203414)
train_sample <- sample(1000,900)
str(train_sample)

credit_train <- credit[train_sample,]
credit_test <- credit[-train_sample,]
str(credit_train)
str(credit_test)

prop.table(table(credit_train$default))
prop.table(table(credit_test$default))

# Installing C5.0 Package
install.packages("C50")
library(C50)

# Training the model
# The expression default ~ . is a R formular interface
# It means modelling default field using (~) . (all predictors)
# Alternatively default might be modeled using saving_balance + credit history
# + amoung (the different predictors are concatenated using the +)
credit_model <- C5.0(default ~ ., data=credit_train)

# Attribute usage refers to the percentage of rows in the training data that use
# the listed feature to make a final prediction. For instance checking_balance
# is used at the very beginning so 100% of the rows are used. At the very last,
# only 0.78% of the examples uses age for decision.
credit_model
summary(credit_model)
plot(credit_model)

# Evaluating model performance
credit_pred <- predict(credit_model,credit_test)
library(gmodels)
CrossTable(credit_test$default, credit_pred, prop.chisq = FALSE, prop.c = FALSE,
           prop.r = FALSE, dnn = c('actual default','predicted default'))

# Improving model performance with boosting i.e many decision trees are build,
# and the trees vote on the best class for each example.
# Interesting reference: Boosting: Foundations and Algorithms
credit_boost25 <- C5.0(default ~ ., data=credit_train, trials=25)
credit_boost25
summary(credit_boost25)

credit_boost_pred25 <- predict(credit_boost25, credit_test)
CrossTable(credit_test$default, credit_boost_pred25,prop.chisq = FALSE,
           prop.c = FALSE, prop.r = FALSE, dnn = c('actual default','predicted
                                                   default'))

# Considering the use case, false negative are most costly than false positive
# In order to reduce this false negatives, a cost matrix is useful
matrix_dimensions <- list(c('no','yes'),c('no','yes'))
names(matrix_dimensions) <- c('predicted','actual')
matrix_dimensions

error_cost <- matrix(c(0,1,4,0), nrow = 2, dimnames = matrix_dimensions)
error_cost

credit_cost <- C5.0(default ~.,data=credit_train,costs=error_cost)
credit_cost_pred <- predict(credit_cost, credit_test)
CrossTable(credit_test$default, credit_cost_pred,prop.chisq = FALSE,
            prop.c = FALSE, prop.r = FALSE, dnn = c('actual default','predicted
                                                   default'))


# Rule Learners
# 1R Algorithms (1 feature i.e. 1 rule): 1R divides the data into groups with
# similar values of the feature. Then, for each segment, the algorithm predicts
# the majority class. The errpr rate for the rule based on each feature is
# calculated and the rule with the fewest errors is choosen as the one rule.
# Strengths
#  - Generates a single, easy-to-understand, human-readable rule.
#  - Often performs surprisingly well
#  - Can serve as a benchmark for more complex algorithms
# Weaknessenes
#  - Use only a single feature
#  - Probably overly simplistic

# The Ripper Algorithm
# The Incremental Reduce Error Pruning (IREP) algorithm uses a combination of
# pre-pruning and post-pruning methods that grow very complex rules and prune
# them before separating the instances fro the full dataset.
# Reference: Incremental Reduced Error Pruning
# The Repeated Incremental Pruning to Produce Error Reduction (RIPPER) algorithm
# improves upon IREP to generate rules that match or exceed the performance of
# decision trees.
# Reference: Fast Effective Rule Induction
# Strengths
#  - Generates easy-to-understand, human-readable rules
#  - Efficient on large and noisy datasets
#  - Generally, produces a simpler model than a comparable decision tree
# Weaknesses
#  - May result in rules that seem to defy common sense or expert knowledge
#  - Not ideal for working with numeric data
#  - Might not perform as well as more complex models.

# Identifying poisonous mushrooms with rule learners
mushrooms <- read.csv("mushrooms.csv", stringsAsFactors = TRUE)
str(mushrooms)

# mushrooms$veil_type is 1 for all the records, and thus it does not provide
# any information
mushrooms$veil_type <- NULL
table(mushrooms$type)

# ZeroR is not useful because all the mushrooms are edible.
# Using 1R
install.packages("OneR")
library(OneR)
mushrooms_1R <- OneR(type ~ ., data=mushrooms)
mushrooms_1R
str(mushrooms$odor)

# Model performance using confusion matrix predicted vs actual
mushroom_1R_pred <- predict(mushrooms_1R, mushrooms)
table(actual=mushrooms$type, predicted=mushroom_1R_pred)

# Improving the model using JRip (Weka https://ml.cms.waikato.ac.nz/weka)
install.packages("RWeka")
library(RWeka)

mushroom_JRip <- JRip(type ~., data=mushrooms)
mushroom_JRip

mushroom_J48 <- J48(type ~ ., data=mushrooms)
mushroom_J48
summary(mushroom_J48) # calls evaluate_Weka_classifier()
table(mushrooms$type, predict(mushroom_J48)) # by hand
