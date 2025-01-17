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

