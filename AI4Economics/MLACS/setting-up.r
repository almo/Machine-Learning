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
# Setting context
#
set.seed(845904523)
Sys.setenv(R_DATATABLE_NUM_THREADS = 8)

#
# Loading dependencies
#
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
