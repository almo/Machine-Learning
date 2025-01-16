#
# Copyright 2025 Andrés Leonardo Martínez Ortiz
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


#
# Universidad Nacional de Educación a Distancia (UNED)
# Grado de Administración y Dirección de Empresas
# Machine Learning Aplicado a Ciencias Sociales Curso 2024/25
# Practicum

# Setting context
set.seed(845904523)
Sys.setenv(R_DATATABLE_NUM_THREADS = 8)

# Loading dependencies
if (!require(ggplot2))
  install.packages("ggplot2")
library(ggplot2)

if (!require(missRanger))
  install.packages('missRanger')
library(missRanger)

if (!require(mltools))
  install.packages("mltools")
library(mltools)

if (!require(data.table))
  install.packages("data.table")
library(data.table)

if (!require(factoextra))
  install.packages("factoextra")
library(factoextra)

if (!require(klaR))
  install.packages("klaR")
library(klaR)

if (!require(cluster))
  install.packages("cluster")
library(cluster)

if (!require(rpart))
  install.packages("rpart")
library(rpart)

if (!require(rpart.plot))
  install.packages("rpart.plot")
library(rpart.plot)

if (!require(C50))
  install.packages("C50")
library(C50)

if (!require(randomForest))
  install.packages("randomForest")

library(randomForest)

if (!require(e1071))
  install.packages("e1071")
library(e1071)


if (!require(gbm))
  install.packages("gbm")
library(gbm)

if (!require(klaR))
  install.packages("klaR")
library(klaR)

# Loading dataset
NFCS.2021 <- read.csv('Datasets/nfcs-2021.csv', stringsAsFactors = TRUE)

# Loading definition and refactoring of data frames
source('demographics.r')
source('capabilities.r')
source('stress.r')

#
# Preprocessing
#

# Missing values
# Demographics
NA.Demographics <- data.frame(
  Feature = character(),
  Missing_Count = numeric(),
  Missing_Percent = numeric(),
  stringsAsFactors = FALSE
)

for (feature in DemographicsFeatures) {
  missing_count <- sum(is.na(NFCS.2021.Demographics[[feature]]))
  missing_percent <- round((missing_count / nrow(NFCS.2021.Demographics)) * 100, 2)
  nr <- nrow(NA.Demographics) + 1
  NA.Demographics[nr, "Feature"] <- feature
  NA.Demographics[nr, "Missing_Count"] <- missing_count
  NA.Demographics[nr, "Missing_Percent"] <- missing_percent
  
}

ggplot(NA.Demographics, aes(x = Feature, y = Missing_Percent)) +
  geom_col(fill = "steelblue") +
  geom_text(aes(label = paste0(Missing_Percent, "%")), vjust = -0.5, size = 3) +
  labs(title = "Variables Demográficas - Porcentaje de Valores Faltantes", x = "Variable", y = "Porcentaje") +
  theme(axis.text.x = element_text(angle = 45, hjust = 1))

print("Demographics features: missing values for A10")
print(table(NFCS.2021.Demographics$A10, useNA = "ifany"))

print("Demographics features: missing values for A21_2015")
print(table(NFCS.2021.Demographics$A21_2015, useNA = "ifany"))

# Missing values
# Capabilities
NA.Capabilities <- data.frame(
  Feature = character(),
  Missing_Count = numeric(),
  Missing_Percent = numeric(),
  stringsAsFactors = FALSE
)

for (feature in CapabilitiesFeatures) {
  missing_count <- sum(is.na(NFCS.2021.Capabilities[[feature]]))
  missing_percent <- round((missing_count / nrow(NFCS.2021.Capabilities)) * 100, 2)
  nr <- nrow(NA.Capabilities) + 1
  NA.Capabilities[nr, "Feature"] <- feature
  NA.Capabilities[nr, "Missing_Count"] <- missing_count
  NA.Capabilities[nr, "Missing_Percent"] <- missing_percent
}

ggplot(NA.Capabilities, aes(x = Feature, y = Missing_Percent)) +
  geom_col(fill = "steelblue") +
  geom_text(aes(label = paste0(Missing_Percent, "%")), vjust = -0.5, size = 3) +
  labs(title = "Variables Alfabetización Financiera - Porcentaje de Valores Faltantes", x = "Variable", y = "Porcentaje") +
  theme(axis.text.x = element_text(angle = 45, hjust = 1))

print("Capabilities features: missing values for C2_2012")
print(table(NFCS.2021.Capabilities$C2_2012, useNA = "ifany"))

print("Capabilities features: missing values for C5_2012")
print(table(NFCS.2021.Capabilities$C5_2012, useNA = "ifany"))

print("Capabilities features: missing values for E7")
print(table(NFCS.2021.Capabilities$E7, useNA = "ifany"))

print("Capabilities features: missing values for B14")
print(table(NFCS.2021.Capabilities$B14, useNA = "ifany"))

# Missing values
# Stress Features
NA.Stress <- data.frame(
  Feature = character(),
  Missing_Count = numeric(),
  Missing_Percent = numeric(),
  stringsAsFactors = FALSE
)

for (feature in StressFeatures) {
  missing_count <- sum(is.na(NFCS.2021.Stress[[feature]]))
  missing_percent <- round((missing_count / nrow(NFCS.2021.Stress)) * 100, 2)
  nr <- nrow(NA.Stress) + 1
  NA.Stress[nr, "Feature"] <- feature
  NA.Stress[nr, "Missing_Count"] <- missing_count
  NA.Stress[nr, "Missing_Percent"] <- missing_percent
}

ggplot(NA.Stress, aes(x = Feature, y = Missing_Percent)) +
  geom_col(fill = "steelblue") +
  geom_text(
    aes(label = paste0(Missing_Percent, "%")),
    hjust = -0.1 ,
    vjust = -0.5,
    size = 3,
    angle = 45
  ) +
  labs(title = "Variables Estrés Financiero - Porcentaje de Valores Faltantes", x = "Variable", y = "Porcentaje") +
  theme(axis.text.x = element_text(angle = 45, hjust = 1))

print("Stress features: missing values for C10_2012")
print(table(NFCS.2021.Stress$C10_2012, useNA = "ifany"))

print("Stress features: missing values for E15_2015")
print(table(NFCS.2021.Stress$E15_2015, useNA = "ifany"))

print("Stress features: missing values for G35")
print(table(NFCS.2021.Stress$G35, useNA = "ifany"))

print("Stress features: missing values for J6")
print(table(NFCS.2021.Stress$J6, useNA = "ifany"))

print("Stress features: missing values for F2_1")
print(table(NFCS.2021.Stress$F2_1, useNA = "ifany"))

print("Stress features: missing values for F2_2")
print(table(NFCS.2021.Stress$F2_2, useNA = "ifany"))

print("Stress features: missing values for F2_3")
print(table(NFCS.2021.Stress$F2_3, useNA = "ifany"))

print("Stress features: missing values for F2_4")
print(table(NFCS.2021.Stress$F2_4, useNA = "ifany"))

print("Stress features: missing values for F2_5")
print(table(NFCS.2021.Stress$F2_5, useNA = "ifany"))

print("Stress features: missing values for F2_6")
print(table(NFCS.2021.Stress$F2_6, useNA = "ifany"))

#
# Missing value treatment
#

# Demographic Features: removing A10 & A21_2015
CompletedDemographicsFeatures <- c(
  "NFCSID",
  "STATEQ",
  "A50A",
  "A3Ar_w",
  "A50B",
  "A5_2015",
  "A6",
  "A7",
  "A11",
  "A8_2021",
  "A9",
  "A40",
  "A41"
)

NFCS.2021.Demographics <- NFCS.2021.Demographics[, CompletedDemographicsFeatures]
str(NFCS.2021.Demographics)

# Financial Capabilities:
#    - removing C2_2012, C5_2012, E7
#    - multiple imputation using missForest B14
CompletedCapabilitiesFeatures <- c(
  'NFCSID',
  'B1',
  'B2',
  'B31',
  'B42',
  'B43',
  'C1_2012',
  'B14',
  'EA_1',
  'F1',
  'H1',
  'M1_1',
  'M4',
  'M20'
)

NFCS.2021.Capabilities <- NFCS.2021.Capabilities[, CompletedCapabilitiesFeatures]
str(NFCS.2021.Capabilities)

# Stress Capabilities:
#    - removing C10_2012, E15_2015, G35, J6
#    - multiple imputation using missForest: F2_1, F2_2, F2_3, F2_4, F2_5, F2_6

CompletedStressFeatures <- c(
  'NFCSID',
  'J1',
  'J3',
  'J4',
  'J5',
  'J10',
  'J20',
  'J32',
  'F2_1',
  'F2_2',
  'F2_3',
  'F2_4',
  'F2_5',
  'F2_6',
  'P50',
  'G20',
  'G38',
  'H30_1',
  'H30_2',
  'H30_3'
)

NFCS.2021.Stress <- NFCS.2021.Stress[, CompletedStressFeatures]
str(NFCS.2021.Stress)

#
# Imputing Values for Financial Capabilities and Financial Stress
#

# Financial Capabilities
# Evaluating Missing Values with missRanger (newest implementation of missForest)
ImputedValues <- missRanger(NFCS.2021.Capabilities,pmm = TRUE, seed = 904584523)

# Analyzing the results
ImputedValuesAnalysis <- rbind(
  data.frame(ImputedValues, source = 'Imputado'),
  data.frame(NFCS.2021.Capabilities, source =  'Original')
)

ggplot(ImputedValuesAnalysis, aes(x = B14, fill = source)) +
  geom_bar(position = "dodge") +
  labs(title = "Comparación Distribución B14 Imputado vs Original", x = "Niveles B14", y = "Número") +
  theme_bw()

# NOTE: missForest modify all the values, including the complete, and thus the 
# data frame has to be imputed completely, and not just the features with missing
# values (NFCS.2021.Capabilities$B14 <- ImputedValues$ximp$B14)
prop.table(table(ImputedValuesAnalysis$B14, ImputedValuesAnalysis$source),
           margin = 2)

# Imputing Assigning the missing values
# NOTE: missForest modify all the values, including the complete, and thus the 
# data frame has to be imputed completely, and not just the features with missing
# values (NFCS.2021.Capabilities$B14 <- ImputedValues$ximp$B14)
NFCS.2021.Capabilities <- ImputedValues

# Financial Stress
# Evaluating Missing Values with missRanger (newest implementation of missForest)
ImputedValues <- missRanger(NFCS.2021.Stress,pmm = TRUE, seed = 452384590)

# Analysing the result for all the variables
for (var in c("F2_1", "F2_2", "F2_3", "F2_4", "F2_5", "F2_6")) {
  # Analyzing the results
  ImputedValuesAnalysis <- rbind(
    data.frame(ImputedValues, source = 'Imputado'),
    data.frame(NFCS.2021.Stress, source = 'Original')
  )
  
  # Create the plot
  print(
    ggplot(ImputedValuesAnalysis, aes(x = .data[[var]], fill = source)) +
      geom_bar(position = "dodge") +
      labs(
        title = paste0("Comparación Distribución ", var, " Imputado vs Original"),
        x = paste0("Niveles ", var),
        y = "Número"
      ) +
      theme_bw()
  )
  
  # Print the proportion table
  # NOTE: missForest modify all the values, including the complete, and thus the 
  # data frame has to be imputed completely, and not just the features with missing
  # values (NFCS.2021.Capabilities$B14 <- ImputedValues$ximp$B14)
  print(prop.table(
    table(ImputedValuesAnalysis[[var]], ImputedValuesAnalysis$source),
    margin = 2
  ))
}

# Assigning missing values
# NOTE: missForest modify all the values, including the complete, and thus the 
# data frame has to be imputed completely, and not just the features with missing
# values (NFCS.2021.Capabilities$B14 <- ImputedValues$ximp$B14)
NFCS.2021.Stress <- ImputedValues


#
# one-hot coding
#

# Demographics
NFCS.2021.Demographics.1H <- one_hot(as.data.table(NFCS.2021.Demographics))

# Financial Capabilities
NFCS.2021.Capabilities.1H <- one_hot(as.data.table(NFCS.2021.Capabilities))

# Financial Stress
NFCS.2021.Stress.1H <- one_hot(as.data.table(NFCS.2021.Stress))

#
# Saving curated data sets
#

# Demographics
saveRDS(NFCS.2021.Demographics,
        "./Datasets/NFCS.2021.Demographics.RData")
saveRDS(NFCS.2021.Demographics.1H,
        "./Datasets/NFCS.2021.Demographics1H.RData")

# Financial Capabilities
saveRDS(NFCS.2021.Capabilities,
        "./Datasets/NFCS.2021.Capabilities.RData")
saveRDS(NFCS.2021.Capabilities.1H,
        "./Datasets/NFCS.2021.Capabilities1H.RData")

# Financial Stress
saveRDS(NFCS.2021.Stress, "./Datasets/NFCS.2021.Stress.RData")
saveRDS(NFCS.2021.Stress.1H, "./Datasets/NFCS.2021.Stress1H.RData")


#
# Clustering Analysis
#

# Financial Stress
# kmodes Clustering
StressTaxonomy <- kmodes(NFCS.2021.Stress[,-1], modes=3)

# Size of the clusters
StressTaxonomy$size

# Centroids
StressTaxonomy$modes

#
# Extending the Financial Stress dataset including the Stress Group
#

StressGroup <- StressTaxonomy$cluster

StressGroup <- factor(
  StressGroup,
  levels = c(1, 2, 3),
  labels = c("No Stress", "Stress Risk", "Stress")
)

# New data frame for supervised learning
NFCS2021MLACS <- merge(NFCS.2021.Demographics,
                       NFCS.2021.Capabilities,
                       by = 'NFCSID',
                       all.x = TRUE)
NFCS2021MLACS <- merge(NFCS2021MLACS,
                       NFCS.2021.Stress,
                       by = 'NFCSID',
                       all.x = TRUE)
NFCS2021MLACS$StressGroup <- StressGroup

# One-hot version of the data frame. SVM will required this encoding.
NFCS2021MLACS1H <- merge(
  NFCS.2021.Demographics1H,
  NFCS.2021.Capabilities1H,
  by = 'NFCSID',
  all.x = TRUE
)
NFCS2021MLACS1H <- merge(NFCS2021MLACS1H,
                         NFCS.2021.Stress1H,
                         by = 'NFCSID',
                         all.x = TRUE)
NFCS2021MLACS1H$StressGroup <- StressGroup

NFCS.2021.Demographics1H$StressGroup <- StressGroup
NFCS.2021.Capabilities1H <- StressGroup

NFCS2021MLACS1H <- one_hot(as.data.table(NFCS2021MLACS1H))
NFCS.2021.Demographics1H <- one_hot(as.data.table(NFCS.2021.Demographics1H))
NFCS.2021.Capabilities1H <- one_hot(as.data.table(NFCS.2021.Capabilities1H))

# Augmented data frame for analysis
NFCS.2021.Demographics$StressGroup <- StressGroup
NFCS.2021.Capabilities$StressGroup <- StressGroup
NFCS.2021.Stress$StressGroup <- StressGroup

saveRDS(NFCS.2021.Demographics,
        "./Datasets/NFCS.2021.Demographics_Stress.RData")
saveRDS(NFCS.2021.Capabilities,
        "./Datasets/NFCS.2021.Capabilities_Stress.RData")
saveRDS(NFCS.2021.Stress,
        "./Datasets/NFCS.2021.Stress_Stress.RData")

stress_counts <- table(NFCS.2021.Stress$StressGroup)
stress_counts <- data.frame(StressGroup = names(stress_counts),
                            n = as.numeric(stress_counts))

ggplot(NFCS.2021.Stress, aes(x = StressGroup)) +
  geom_bar(fill = "steelblue") +  # Use a light blue color for the bars
  geom_text(data = stress_counts, aes(label = n, y = n), vjust = -0.5) +
  labs(title = "Distribución del Nivel de Estrés", x = "Nivel de Estrés", y = "Número de Individuos") +
  theme_minimal()

#
# Demographic Analysis
#
StressGroupsState <- table(NFCS.2021.Demographics$StressGroup,
                           NFCS.2021.Demographics$STATEQ)

barplot(
  StressGroupsState,
  beside = TRUE,
  xlab = "Nivel de Estrés",
  ylab = "Numero de Individups",
  main = "Nivel de Estrés por Estado"
)

StressGroupsIncome <- table(NFCS.2021.Demographics$StressGroup,
                            NFCS.2021.Demographics$A8_2021)

barplot(
  StressGroupsIncome,
  beside = TRUE,
  xlab = "Nivel de Estrés",
  ylab = "Numero de Individups",
  main = "Nivel de Estrés por Ingresos"
)

IncomeStressGroups <- table(NFCS.2021.Demographics$A8_2021,
                            NFCS.2021.Demographics$StressGroup)

barplot(
  IncomeStressGroups,
  beside = TRUE,
  xlab = "Nivel de Estrés",
  ylab = "Numero de Individups",
  main = "Ingresos por Nivel de Estrés"
)

#
# Classification
#

# Full data set
NFCS2021MLACS_sample <- sample(nrow(NFCS2021MLACS), nrow(NFCS2021MLACS) *
                                 0.7)
NFCS2021MLACS_train <- NFCS2021MLACS[NFCS2021MLACS_sample, ]
NFCS2021MLACS_test <- NFCS2021MLACS[-NFCS2021MLACS_sample, ]
prop.table(table(NFCS2021MLACS_train$StressGroup))
prop.table(table(NFCS2021MLACS_test$StressGroup))

NFCS2021MLACS_model <- rpart(StressGroup ~ A6 + A11, data = NFCS2021MLACS_train, method =
                               "class")
rpart.plot(NFCS2021MLACS_model, extra = 104, nn = TRUE)
predictions <- predict(NFCS2021MLACS_model, NFCS2021MLACS_test, type = "class")
table(predictions, NFCS2021MLACS_test$StressGroup)

# Random Forest
rf_model <- randomForest(StressGroup ~ A11 + A6 + B1 + B2 + M20, data = NFCS2021MLACS_train)
print(rf_model)
importance(rf_model)
varImpPlot(rf_model)

# SVM
svm_model <- svm(
  StressGroup ~ A11 + A6 + B1 + B2 + M20,
  data = NFCS2021MLACS_train,
  kernel = "sigmoid",
  cost = 10,
  gamma = 0.5
)
summary(svm_model)

predictions <- predict(svm_model, newdata = NFCS2021MLACS_test)
table(predictions, NFCS2021MLACS_test$StressGroup)

#tuned_model <- tune(
#  svm,
#  StressGroup ~ A11 + A6 + B1 + B2 + M20,
#  data = NFCS2021MLACS_train,
#  ranges = list(cost = c(0.1, 1, 10), gamma = c(0.1, 0.5, 1))
#)



# naiveBayes
nb_model <- naiveBayes(StressGroup ~ A11 + A6 + B1 + B2 + M20, data = NFCS2021MLACS_train)
summary(nb_model)

predictions <- predict(nb_model, newdata = NFCS2021MLACS_test)
table(predictions, NFCS2021MLACS_test$StressGroup)

# GBM
gbm_model <- gbm(
  StressGroup ~ A11 + A6 + B1 + B2 + M20,
  data = NFCS2021MLACS,
  distribution = "gaussian",
  n.trees = 100,
  interaction.depth = 3
)

summary(gbm_model)

predictions <- predict(gbm_model, newdata = NFCS2021MLACS_test)
table(predictions, NFCS2021MLACS_test$StressGroup)
