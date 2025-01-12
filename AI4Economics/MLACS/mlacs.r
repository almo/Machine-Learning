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

set.seed(845904523)
Sys.setenv(R_DATATABLE_NUM_THREADS = 8)

# Loading dependencies
if (!require(ggplot2))
  install.packages("ggplot2")
library(ggplot2)

if (!require(missForest))
  install.packages('missForest')
library(missForest)

if (!require(mltools))
  install.packages("mltools")
library(mltools)

if (!require(data.table))
  install.packages("data.table")
library(data.table)

if (!require(factoextra))
  install.packages("factoextra")
library(factoextra)


# Loading dataset
NFCS2021 <- read.csv('Datasets/nfcs-2021.csv', stringsAsFactors = TRUE)

# Loading definition and refactoring of data frames
source('demographics.r')
source('capabilities.r')
source('stress.r')

#
# Preprocessing
#

# Missing values
# Demographics
DemographicsNA <- data.frame(
  Feature = character(),
  Missing_Count = numeric(),
  Missing_Percent = numeric(),
  stringsAsFactors = FALSE
)

for (feature in DemographicsFeatures) {
  missing_count <- sum(is.na(NFCS2021Demographics[[feature]]))
  missing_percent <- round((missing_count / nrow(NFCS2021Demographics)) * 100, 2)
  DemographicsNA[nrow(DemographicsNA) + 1, ] <- c(feature, missing_count, missing_percent)
}

ggplot(DemographicsNA, aes(x = Feature, y = Missing_Percent)) +
  geom_col(fill = "steelblue") +
  geom_text(aes(label = paste0(Missing_Percent, "%")), vjust = -0.5, size = 3) +
  labs(title = "Variables Demográficas - Porcentaje de Valores Faltantes", x = "Variable", y = "Porcentaje") +
  theme(axis.text.x = element_text(angle = 45, hjust = 1))

# Missing values
# Capabilities
CapabilitiesNA <- data.frame(
  Feature = character(),
  Missing_Count = numeric(),
  Missing_Percent = numeric(),
  stringsAsFactors = FALSE
)

for (feature in CapabilitiesFeatures) {
  missing_count <- sum(is.na(NFCS2021Capabilities[[feature]]))
  missing_percent <- round((missing_count / nrow(NFCS2021Capabilities)) * 100, 2)
  CapabilitiesNA[nrow(CapabilitiesNA) + 1, ] <- c(feature, missing_count, missing_percent)
}

ggplot(CapabilitiesNA, aes(x = Feature, y = Missing_Percent)) +
  geom_col(fill = "steelblue") +
  geom_text(aes(label = paste0(Missing_Percent, "%")), vjust = -0.5, size = 3) +
  labs(title = "Variables Alfabetización Financiera - Porcentaje de Valores Faltantes", x = "Variable", y = "Porcentaje") +
  theme(axis.text.x = element_text(angle = 45, hjust = 1))


# Missing values
# Stress Features
StressNA <- data.frame(
  Feature = character(),
  Missing_Count = numeric(),
  Missing_Percent = numeric(),
  stringsAsFactors = FALSE
)

for (feature in StressFeatures) {
  missing_count <- sum(is.na(NFCS2021Stress[[feature]]))
  missing_percent <- round((missing_count / nrow(NFCS2021Stress)) * 100, 2)
  StressNA[nrow(StressNA) + 1, ] <- c(feature, missing_count, missing_percent)
}

ggplot(StressNA, aes(x = Feature, y = Missing_Percent)) +
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

print("Demographics features: missing values for A10")
print(table(NFCS2021Demographics$A10, useNA = "ifany"))

print("Demographics features: missing values for A21_2015")
print(table(NFCS2021Demographics$A21_2015, useNA = "ifany"))

NFCS2021Demographics <- NFCS2021Demographics[, CompletedDemographicsFeatures]
str(NFCS2021Demographics)

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

print("Capabilities features: missing values for C2_2012")
print(table(NFCS2021Capabilities$C2_2012, useNA = "ifany"))

print("Capabilities features: missing values for C5_2012")
print(table(NFCS2021Capabilities$C5_2012, useNA = "ifany"))

print("Capabilities features: missing values for E7")
print(table(NFCS2021Capabilities$E7, useNA = "ifany"))

print("Capabilities features: missing values for B14")
print(table(NFCS2021Capabilities$B14, useNA = "ifany"))

NFCS2021Capabilities <- NFCS2021Capabilities[, CompletedCapabilitiesFeatures]
str(NFCS2021Capabilities)

# Imputing Missing Values with missForests
ImputedValues <- missForest(NFCS2021Capabilities)

# Analyzing the results
ImputedValuesAnalysis <- rbind(
  data.frame(ImputedValues$ximp, source = 'Imputado'),
  data.frame(NFCS2021Capabilities, source =  'Original')
)

ggplot(ImputedValuesAnalysis, aes(x = B14,  fill = source)) +
  geom_bar(position = "dodge") +
  labs(title = "Comparación Distribución B14 Imputado vs Original", x = "Niveles B14", y = "Número") +
  theme_bw()

prop.table(table(ImputedValuesAnalysis$B14, ImputedValuesAnalysis$source),
           margin = 2)

# Assigning the missing values
NFCS2021Capabilities$B14 <- ImputedValues$ximp$B14

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

print("Stress features: missing values for C10_2012")
print(table(NFCS2021Stress$C10_2012, useNA = "ifany"))

print("Stress features: missing values for E15_2015")
print(table(NFCS2021Stress$E15_2015, useNA = "ifany"))

print("Stress features: missing values for G35")
print(table(NFCS2021Stress$G35, useNA = "ifany"))

print("Stress features: missing values for J6")
print(table(NFCS2021Stress$J6, useNA = "ifany"))

print("Stress features: missing values for F2_1")
print(table(NFCS2021Stress$F2_1, useNA = "ifany"))

print("Stress features: missing values for F2_2")
print(table(NFCS2021Stress$F2_2, useNA = "ifany"))

print("Stress features: missing values for F2_3")
print(table(NFCS2021Stress$F2_3, useNA = "ifany"))

print("Stress features: missing values for F2_4")
print(table(NFCS2021Stress$F2_4, useNA = "ifany"))

print("Stress features: missing values for F2_5")
print(table(NFCS2021Stress$F2_5, useNA = "ifany"))

print("Stress features: missing values for F2_6")
print(table(NFCS2021Stress$F2_6, useNA = "ifany"))

NFCS2021Stress <- NFCS2021Stress[, CompletedStressFeatures]
str(NFCS2021Stress)
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
# Imputing missing values with missForest
ImputedValues <- missForest(NFCS2021Stress)

# Analysing the result for all the variables
for (var in c("F2_1", "F2_2", "F2_3", "F2_4", "F2_5", "F2_6")) {
  # Analyzing the results
  ImputedValuesAnalysis <- rbind(
    data.frame(ImputedValues$ximp, source = 'Imputado'),
    data.frame(NFCS2021Stress, source = 'Original')
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
  print(prop.table(
    table(ImputedValuesAnalysis[[var]], ImputedValuesAnalysis$source),
    margin = 2
  ))
  
  # Assigning missing values
  NFCS2021Stress[[var]] <- ImputedValues$ximp[[var]]
}

#
# one-hot coding
#
if (!require(data.table))
  install.packages("data.table")
library(data.table)

# Demographics
NFCS2021Demographics1H <- one_hot(as.data.table(NFCS2021Demographics))

# Financial Capabilities
NFCS2021Capabilities1H <- one_hot(as.data.table(NFCS2021Capabilities))

# Financial Stress 
NFCS2021Stress1H <- one_hot(as.data.table(NFCS2021Stress))
