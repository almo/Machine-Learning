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

# Loading dependencies
if (!require(ggplot2))
  install.packages("ggplot2")
library(ggplot2)

if (!require(missForest))
  install.packages('missForest')
library(missForest)

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

NFCS2021Capabilities <- NFCS2021Capabilities[, CompletedCapabilitiesFeatures]
str(NFCS2021Capabilities)

# Imputing Missing Values with missForests
ImputedValues <- missForest(NFCS2021Capabilities)

# Analyzing the results
ImputedValuesAnalysis <- rbind(
  data.frame(ImputedValues$ximp, source = 'Imputado'),
  data.frame(NFCS2021Capabilities, source =  'Original')
)

ggplot(ImputedValuesAnalysis, aes(x = B14, fill = source)) +
  geom_bar(position = "dodge") +
  labs(title = "Comparación Distribución B14 Imputado vs Original",
       x = "Niveles B14",
       y = "Número") +
  theme_bw()

prop.table(table(ImputedValuesAnalysis$B14, ImputedValuesAnalysis$source), margin = 2)

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

NFCS2021Stress <- NFCS2021Stress[, CompletedStressFeatures]
str(NFCS2021Stress)

# Imputing missing values with missForest
ImputedValues <- missForest(NFCS2021Stress)

# Analyzing the results for F2_1
ImputedValuesAnalysis <- rbind(
  data.frame(ImputedValues$ximp, source = 'Imputado'),
  data.frame(NFCS2021Stress, source =  'Original')
)

ggplot(ImputedValuesAnalysis, aes(x = F2_1, fill = source)) +
  geom_bar(position = "dodge") +
  labs(title = "Comparación Distribución F2_1 Imputado vs Original",
       x = "Niveles F2_1",
       y = "Número") +
  theme_bw()

prop.table(table(ImputedValuesAnalysis$F2_1, ImputedValuesAnalysis$source), margin = 2)

# Assigning missing values for F2_1
NFCS2021Stress$F2_1 <- ImputedValues$ximp$F2_1

# Analyzing the results for F2_2
ImputedValuesAnalysis <- rbind(
  data.frame(ImputedValues$ximp, source = 'Imputado'),
  data.frame(NFCS2021Stress, source =  'Original')
)

ggplot(ImputedValuesAnalysis, aes(x = F2_2, fill = source)) +
  geom_bar(position = "dodge") +
  labs(title = "Comparación Distribución F2_2 Imputado vs Original",
       x = "Niveles F2_2",
       y = "Número") +
  theme_bw()

prop.table(table(ImputedValuesAnalysis$F2_2, ImputedValuesAnalysis$source), margin = 2)

# Assigning missing values for F2_2
NFCS2021Stress$F2_2 <- ImputedValues$ximp$F2_2

# Analyzing the results for F2_3
ImputedValuesAnalysis <- rbind(
  data.frame(ImputedValues$ximp, source = 'Imputado'),
  data.frame(NFCS2021Stress, source =  'Original')
)

ggplot(ImputedValuesAnalysis, aes(x = F2_3, fill = source)) +
  geom_bar(position = "dodge") +
  labs(title = "Comparación Distribución F2_3 Imputado vs Original",
       x = "Niveles F2_3",
       y = "Número") +
  theme_bw()

prop.table(table(ImputedValuesAnalysis$F2_3, ImputedValuesAnalysis$source), margin = 2)

# Assigning missing values for F2_3
NFCS2021Stress$F2_3 <- ImputedValues$ximp$F2_3

# Analyzing the results for F2_4
ImputedValuesAnalysis <- rbind(
  data.frame(ImputedValues$ximp, source = 'Imputado'),
  data.frame(NFCS2021Stress, source =  'Original')
)

ggplot(ImputedValuesAnalysis, aes(x = F2_4, fill = source)) +
  geom_bar(position = "dodge") +
  labs(title = "Comparación Distribución F2_4 Imputado vs Original",
       x = "Niveles F2_4",
       y = "Número") +
  theme_bw()

prop.table(table(ImputedValuesAnalysis$F2_4, ImputedValuesAnalysis$source), margin = 2)

# Assigning missing values for F2_5
NFCS2021Stress$F2_5 <- ImputedValues$ximp$F2_5

# Analyzing the results for F2_5
ImputedValuesAnalysis <- rbind(
  data.frame(ImputedValues$ximp, source = 'Imputado'),
  data.frame(NFCS2021Stress, source =  'Original')
)

ggplot(ImputedValuesAnalysis, aes(x = F2_5, fill = source)) +
  geom_bar(position = "dodge") +
  labs(title = "Comparación Distribución F2_5 Imputado vs Original",
       x = "Niveles F2_5",
       y = "Número") +
  theme_bw()

prop.table(table(ImputedValuesAnalysis$F2_5, ImputedValuesAnalysis$source), margin = 2)

# Assigning missing values for F2_5
NFCS2021Stress$F2_5 <- ImputedValues$ximp$F2_5

# Analyzing the results for F2_6
ImputedValuesAnalysis <- rbind(
  data.frame(ImputedValues$ximp, source = 'Imputado'),
  data.frame(NFCS2021Stress, source =  'Original')
)

ggplot(ImputedValuesAnalysis, aes(x = F2_6, fill = source)) +
  geom_bar(position = "dodge") +
  labs(title = "Comparación Distribución F2_6 Imputado vs Original",
       x = "Niveles F2_6",
       y = "Número") +
  theme_bw()

prop.table(table(ImputedValuesAnalysis$F2_6, ImputedValuesAnalysis$source), margin = 2)

# Assigning missing values for F2_6
NFCS2021Stress$F2_6 <- ImputedValues$ximp$F2_6
