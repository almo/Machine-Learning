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
if (!require(FactoMineR))
  install.packages("FactoMinerR")
library(FactoMineR)

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

# Loading dataset
NFCS.2021 <- read.csv('Datasets/nfcs-2021.csv', stringsAsFactors = TRUE)

# Loading definition and refactoring of data frames
source('demographics.r')
source('capabilities.r')
source('stress.r')

#
# Preprocessing
#

#
# Apriori
# Principal component analysis for raw data
# Multiple Correspondence Analysis
#

# Demographics
nrows <- nrow(NFCS.2021.Demographics)
NFCS.2021.Demographics.PA.Sample <- sample(nrows,nrows*0.1)
NFCS.2021.Demographics.PA <- NFCS.2021.Demographics[NFCS.2021.Demographics.PA.Sample,-1]

NFCS.2021.Demographics.PA.Result <- MCA(NFCS.2021.Demographics.PA)
fviz_mca_biplot(NFCS.2021.Demographics.PA.Result)
fviz_mca_ind(NFCS.2021.Demographics.PA.Result)
fviz_eig(NFCS.2021.Demographics.PA.Result)
fviz_mca_var(NFCS.2021.Demographics.PA.Result)

# Financial Capabilities
nrows <- nrow(NFCS.2021.Capabilities)
NFCS.2021.Capabilities.PA.Sample <- sample(nrows,nrows*0.1)
NFCS.2021.Capabilities.PA <- NFCS.2021.Capabilities[NFCS.2021.Capabilities.PA.Sample,-1]

NFCS.2021.Capabilities.PA.Result <- MCA(NFCS.2021.Capabilities.PA)
fviz_mca_biplot(NFCS.2021.Capabilities.PA.Result)
fviz_mca_ind(NFCS.2021.Capabilities.PA.Result)
fviz_eig(NFCS.2021.Capabilities.PA.Result)
fviz_mca_var(NFCS.2021.Capabilities.PA.Result)

# Financial Stress
nrows <- nrow(NFCS.2021.Stress)
NFCS.2021.Stress.PA.Sample <- sample(nrows,nrows*0.1)
NFCS.2021.Stress.PA <- NFCS.2021.Stress[NFCS.2021.Stress.PA.Sample,-1]

NFCS.2021.Stress.PA.Result <- MCA(NFCS.2021.Stress.PA)
fviz_mca_biplot(NFCS.2021.Capabilities.PA.Result)
fviz_mca_ind(NFCS.2021.Capabilities.PA.Result)
fviz_eig(NFCS.2021.Capabilities.PA.Result)
fviz_mca_var(NFCS.2021.Capabilities.PA.Result)

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
ImputedValues <- missRanger(NFCS.2021.Capabilities, pmm = TRUE, seed = 904584523)

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
ImputedValues <- missRanger(NFCS.2021.Stress, pmm = TRUE, seed = 452384590)

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
# A posteriori 
# Principal component analysis for raw data
# Multiple Correspondence Analysis
#
# Demographics
nrows <- nrow(NFCS.2021.Demographics)
NFCS.2021.Demographics.PA.Sample <- sample(nrows,nrows*0.1)
NFCS.2021.Demographics.PA <- NFCS.2021.Demographics[NFCS.2021.Demographics.PA.Sample,-1]

NFCS.2021.Demographics.PA.Result <- MCA(NFCS.2021.Demographics.PA)
fviz_mca_biplot(NFCS.2021.Demographics.PA.Result)
fviz_mca_ind(NFCS.2021.Demographics.PA.Result)
fviz_eig(NFCS.2021.Demographics.PA.Result)
fviz_mca_var(NFCS.2021.Demographics.PA.Result)

# Financial Capabilities
nrows <- nrow(NFCS.2021.Capabilities)
NFCS.2021.Capabilities.PA.Sample <- sample(nrows,nrows*0.1)
NFCS.2021.Capabilities.PA <- NFCS.2021.Capabilities[NFCS.2021.Capabilities.PA.Sample,-1]

NFCS.2021.Capabilities.PA.Result <- MCA(NFCS.2021.Capabilities.PA)
fviz_mca_biplot(NFCS.2021.Capabilities.PA.Result)
fviz_mca_ind(NFCS.2021.Capabilities.PA.Result)
fviz_eig(NFCS.2021.Capabilities.PA.Result)
fviz_mca_var(NFCS.2021.Capabilities.PA.Result)

# Financial Stress
nrows <- nrow(NFCS.2021.Stress)
NFCS.2021.Stress.PA.Sample <- sample(nrows,nrows*0.1)
NFCS.2021.Stress.PA <- NFCS.2021.Stress[NFCS.2021.Stress.PA.Sample,-1]

NFCS.2021.Stress.PA.Result <- MCA(NFCS.2021.Stress.PA)
fviz_mca_biplot(NFCS.2021.Capabilities.PA.Result)
fviz_mca_ind(NFCS.2021.Capabilities.PA.Result)
fviz_eig(NFCS.2021.Capabilities.PA.Result)
fviz_mca_var(NFCS.2021.Capabilities.PA.Result)

#
# Clustering Analysis
#

# Financial Stress
# kmodes Clustering
StressTaxonomy <- kmodes(NFCS.2021.Stress[, -1], modes = 3)

# Size of the clusters
StressTaxonomy$size

# Centroids
StressTaxonomy$modes

# Distance
StressTaxonomy$withindiff

#
# Extending the Financial Stress dataset including the Stress Group
#
StressGroup <- StressTaxonomy$cluster

StressGroup <- factor(
  StressGroup,
  levels = c(3, 2, 1),
  labels = c("No Stress", "Stress Risk", "Stress")
)

# New data frame for supervised learning
NFCS.2021.MLACS <- merge(NFCS.2021.Demographics,
                         NFCS.2021.Capabilities,
                         by = 'NFCSID',
                         all.x = TRUE)

NFCS.2021.MLACS <- merge(NFCS.2021.MLACS,
                         NFCS.2021.Stress,
                         by = 'NFCSID',
                         all.x = TRUE)

NFCS.2021.MLACS$StressGroup <- StressGroup

# Demographics
NFCS.2021.Demographics$StressGroup <- StressGroup

# Financial Capabilities
NFCS.2021.Capabilities$StressGroup <- StressGroup

# Financial Stress
NFCS.2021.Stress$StressGroup <- StressGroup

#
# Analysis of the Stress Groups and economical analysis
#
StressCounts <- table(NFCS.2021.Stress$StressGroup)
StressCounts <- data.frame(StressGroup = names(StressCounts),
                           n = as.numeric(StressCounts))

ggplot(NFCS.2021.Stress, aes(x = StressGroup)) +
  geom_bar(fill = "steelblue") +  # Use a light blue color for the bars
  geom_text(data = StressCounts, aes(label = n, y = n), vjust = -0.5) +
  labs(title = "Distribución del Nivel de Estrés", x = "Nivel de Estrés", y = "Número de Individuos") +
  theme_minimal()

# Demographic Analysis
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
# Modelling StressGroup using Demographics information
# A50A     Gender
# A3Ar_w   Age group
# A6       Marital Status
# A11      Number of children
# A9       Work Status
# A8_2021  Income
# A5_2015  Education

# Training / Testing dataset
nrows <- nrow(NFCS.2021.MLACS)
NFCS.2021.MLACS.Sample <- sample(nrows, nrows * 0.7)
NFCS.2021.MLACS.Train <- NFCS.2021.MLACS[NFCS.2021.MLACS.Sample, ]
NFCS.2021.MLACS.Test <- NFCS.2021.MLACS[-NFCS.2021.MLACS.Sample, ]

prop.table(table(NFCS.2021.MLACS.Train$StressGroup))
prop.table(table(NFCS.2021.MLACS.Test$StressGroup))

#
# Decision Tree
#
NFCS.2021.MLACS.D.DT.Model <- rpart(
  StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015,
  data = NFCS.2021.MLACS.Train,
  method = "class"
)

rpart.plot(NFCS.2021.MLACS.D.DT.Model, extra = 104, nn = TRUE)
NFCS.2021.MLACS.D.DT.Model.Prune <- prune(NFCS.2021.MLACS.D.DT.Model, cp = 0.01)  # Example cp value
rpart.plot(NFCS.2021.MLACS.D.DT.Model.Prune,
           extra = 104,
           nn = TRUE)

predictions.dt.d <- predict(NFCS.2021.MLACS.D.DT.Model, NFCS.2021.MLACS.Test, type = "class")
table(predictions.dt.d, NFCS.2021.MLACS.Test$StressGroup)

# Random Forest
NFCS.2021.MLACS.D.RF.Model <- randomForest(StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015, data = NFCS.2021.MLACS.Train)
print(NFCS.2021.MLACS.D.RF.Model)
plot(NFCS.2021.MLACS.D.RF.Model)
importance(NFCS.2021.MLACS.D.RF.Model)
varImpPlot(NFCS.2021.MLACS.D.RF.Model)

predictions.rf.d <- predict(NFCS.2021.MLACS.D.RF.Model, NFCS.2021.MLACS.Test, type = "class")
table(predictions.rf.d, NFCS.2021.MLACS.Test$StressGroup)

# Support Vector Machine
NFCS.2021.MLACS.D.SVM.Model <- svm(
  StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015,
  data = NFCS.2021.MLACS.Train,
  kernel = "radial",
  cost = 10,
  gamma = 0.5
)
summary(NFCS.2021.MLACS.D.SVM.Model)

predictions.svm.d <- predict(NFCS.2021.MLACS.D.SVM.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.svm.d, NFCS.2021.MLACS.Test$StressGroup)

# Naive Bayes
NFCS.2021.MLACS.D.NB.Model <- naiveBayes(StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015, data = NFCS.2021.MLACS.Train)
summary(NFCS.2021.MLACS.D.NB.Model)

predictions.nb.d <- predict(NFCS.2021.MLACS.D.NB.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.nb.d, NFCS.2021.MLACS.Test$StressGroup)

# Gradient Boosting Machine
NFCS.2021.MLACS.D.GBM.Model <- gbm(
  StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015,
  data = NFCS.2021.MLACS,
  distribution = "gaussian",
  n.trees = 100,
  interaction.depth = 3
)

summary(NFCS.2021.MLACS.D.GBM.Model)

predictions.gbm.d <- predict(NFCS.2021.MLACS.D.GBM.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.gbm.d, NFCS.2021.MLACS.Test$StressGroup)

#
# Classification
#
# Modelling StressGroup using Financial Capabilities information
# B1       Checking Account
# B2       Saving Account
# B31      Mobile Payment
# B42      Mobile Operation (transfers)
# B43      Financial planning (Web/Mobile)
# C1_2012  Retirement plans
# B14      Investment
# EA_1     Owned house
# F1       Number of credit cards
# M4       Financial knowledge
#
# Decision Tree
#
NFCS.2021.MLACS.C.DT.Model <- rpart(
  StressGroup ~ B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
  data = NFCS.2021.MLACS.Train,
  method = "class"
)
summary(NFCS.2021.MLACS.C.DT.Model)
rpart.plot(NFCS.2021.MLACS.C.DT.Model, extra = 104, nn = TRUE)

predictions.dt.c <- predict(NFCS.2021.MLACS.C.DT.Model, NFCS.2021.MLACS.Test, type = "class")
table(predictions.dt.c, NFCS.2021.MLACS.Test$StressGroup)

# Random Forest
NFCS.2021.MLACS.C.RF.Model <- randomForest(StressGroup ~ B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
                                         data = NFCS.2021.MLACS.Train)
summary(NFCS.2021.MLACS.C.RF.Model)
plot(NFCS.2021.MLACS.C.RF.Model)
importance(NFCS.2021.MLACS.C.RF.Model)
varImpPlot(NFCS.2021.MLACS.C.RF.Model)

predictions.rf.c <- predict(NFCS.2021.MLACS.C.RF.Model, NFCS.2021.MLACS.Test, type = "class")
table(predictions.rf.c, NFCS.2021.MLACS.Test$StressGroup)

# Support Vector Machine
NFCS.2021.MLACS.C.SVM.Model <- svm(
  StressGroup ~ B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
  data = NFCS.2021.MLACS.Train,
  kernel = "radial",
  cost = 10,
  gamma = 0.5
)
summary(NFCS.2021.MLACS.C.SVM.Model)

predictions.svm.c <- predict(NFCS.2021.MLACS.C.SVM.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.svm.c, NFCS.2021.MLACS.Test$StressGroup)

# Naive Bayes
NFCS.2021.MLACS.C.NB.Model <- naiveBayes(StressGroup ~ B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
                                       data = NFCS.2021.MLACS.Train)
summary(NFCS.2021.MLACS.C.NB.Model)

predictions.nb.c <- predict(NFCS.2021.MLACS.C.NB.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.nb.c, NFCS.2021.MLACS.Test$StressGroup)

# Gradient Boosting Machine
NFCS.2021.MLACS.C.GBM.Model <- gbm(
  StressGroup ~ B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
  data = NFCS.2021.MLACS,
  distribution = "gaussian",
  n.trees = 100,
  interaction.depth = 3
)

summary(NFCS.2021.MLACS.C.GBM.Model)

predictions.gbm.c <- predict(NFCS.2021.MLACS.C.GBM.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.gbm.c, NFCS.2021.MLACS.Test$StressGroup)


#
# Classification
#
# Modelling StressGroup using Demographic and Financial Capabilities information 
# A50A     Gender
# A3Ar_w   Age group
# A6       Marital Status
# A11      Number of children
# A9       Work Status
# A8_2021  Income
# A5_2015  Education
# B1       Checking Account
# B2       Saving Account
# B31      Mobile Payment
# B42      Mobile Operation (transfers)
# B43      Financial planning (Web/Mobile)
# C1_2012  Retirement plans
# B14      Investment
# EA_1     Owned house
# F1       Number of credit cards
# M4       Financial knowledge
#
# Decision Tree
#
NFCS.2021.MLACS.DT.Model <- rpart(
  StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015 + B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
  data = NFCS.2021.MLACS.Train,
  method = "class"
)
summary(NFCS.2021.MLACS.DT.Model)
rpart.plot(NFCS.2021.MLACS.DT.Model, extra = 104, nn = TRUE)

predictions.dt <- predict(NFCS.2021.MLACS.DT.Model, NFCS.2021.MLACS.Test, type = "class")
table(predictions.dt, NFCS.2021.MLACS.Test$StressGroup)

# Random Forest
NFCS.2021.MLACS.RF.Model <- randomForest(StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015 + B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
                                           data = NFCS.2021.MLACS.Train)
summary(NFCS.2021.MLACS.RF.Model)
plot(NFCS.2021.MLACS.RF.Model)
importance(NFCS.2021.MLACS.RF.Model)
varImpPlot(NFCS.2021.MLACS.RF.Model)

predictions.rf <- predict(NFCS.2021.MLACS.RF.Model, NFCS.2021.MLACS.Test, type = "class")
table(predictions.rf, NFCS.2021.MLACS.Test$StressGroup)

# Support Vector Machine
NFCS.2021.MLACS.SVM.Model <- svm(
  StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015 + B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
  data = NFCS.2021.MLACS.Train,
  kernel = "radial",
  cost = 10,
  gamma = 0.5
)
summary(NFCS.2021.MLACS.SVM.Model)

predictions.svm <- predict(NFCS.2021.MLACS.SVM.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.svm, NFCS.2021.MLACS.Test$StressGroup)

# Naive Bayes
NFCS.2021.MLACS.NB.Model <- naiveBayes(StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015 + B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
                                         data = NFCS.2021.MLACS.Train)
summary(NFCS.2021.MLACS.NB.Model)

predictions.nb <- predict(NFCS.2021.MLACS.NB.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.nb, NFCS.2021.MLACS.Test$StressGroup)

# Gradient Boosting Machine
NFCS.2021.MLACS.GBM.Model <- gbm(
  StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015 + B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
  data = NFCS.2021.MLACS,
  distribution = "gaussian",
  n.trees = 100,
  interaction.depth = 3
)

summary(NFCS.2021.MLACS.GBM.Model)

predictions.gbm <- predict(NFCS.2021.MLACS.GBM.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.gbm, NFCS.2021.MLACS.Test$StressGroup)
