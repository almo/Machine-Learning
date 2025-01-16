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
# Financial Stress Features
#
StressFeatures <- c(
  'NFCSID',
  'J1',
  'J3',
  'J4',
  'J5',
  'J6',
  'J10',
  'J20',
  'J32',
  'C10_2012',
  'E15_2015',
  'F2_1',
  'F2_2',
  'F2_3',
  'F2_4',
  'F2_5',
  'F2_6',
  'P50',
  'G20',
  'G35',
  'G38',
  'H30_1',
  'H30_2',
  'H30_3'
)
str(StressFeatures)
NFCS.2021.Stress <- NFCS.2021[, StressFeatures]
str(NFCS.2021.Stress)

#
# Financial Stress Features
#
# Convert J1 to a factor
# Overall, thinking of your assets, debts and savings, how satisfied are you with
# your current personal financial condition?
NFCS.2021.Stress$J1 <- factor(
  NFCS.2021.Stress$J1,
  levels = c(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 98, 99),
  labels = c(
    "1 - Not At All Satisfied",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7",
    "8",
    "9",
    "10 - Extremely Satisfied",
    "Don't know",
    "Prefer not to say"
  )
)

# Convert J3 to a factor
# Over the past year, would you say your [household's] spending was less than,
# more than, or about equal to your [household's] income?
NFCS.2021.Stress$J3 <- factor(
  NFCS.2021.Stress$J3,
  levels = c(1, 2, 3, 98, 99),
  labels = c(
    "Spending less than income",
    "Spending more than income",
    "Spending about equal to income",
    "Don't know",
    "Prefer not to say"
  )
)

# Convert J4 to a factor
# In a typical month, how difficult is it for you to cover your expenses and
# pay all your bills?
NFCS.2021.Stress$J4 <- factor(
  NFCS.2021.Stress$J4,
  levels = c(1, 2, 3, 98, 99),
  labels = c(
    "Very difficult",
    "Somewhat difficult",
    "Not at all difficult",
    "Don't know",
    "Prefer not to say"
  )
)

# Convert J5 to a factor
# Have you set aside emergency or rainy day funds that would cover your expenses
# for 3 months, in case of sickness, job loss, economic downturn, or other emergencies?
NFCS.2021.Stress$J5 <- factor(
  NFCS.2021.Stress$J5,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert J6 to a factor
# Are you setting aside any money for your children's college education?
NFCS.2021.Stress$J6 <- factor(
  NFCS.2021.Stress$J6,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert J10 to a factor
# In the past 12 months, have you [has your household] experienced a large drop
# in income which you did not expect?
NFCS.2021.Stress$J10 <- factor(
  NFCS.2021.Stress$J10,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert J20 to a factor
# How confident are you that you could come up with $2,000 if an unexpected need
# arose within the next month?
NFCS.2021.Stress$J20 <- factor(
  NFCS.2021.Stress$J20,
  levels = c(1, 2, 3, 4, 98, 99),
  labels = c(
    "I am certain I could come up with the full $2,000",
    "I could probably come up with $2,000",
    "I could probably not come up with $2,000",
    "I am certain I could not come up with $2,000",
    "Don't know",
    "Prefer not to say"
  )
)

# Convert J32 to a factor
# How would you rate your current credit record?
NFCS.2021.Stress$J32 <- factor(
  NFCS.2021.Stress$J32,
  levels = c(1, 2, 3, 4, 5, 98, 99),
  labels = c(
    "Very bad",
    "Bad",
    "About average",
    "Good",
    "Very good",
    "Don't know",
    "Prefer not to say"
  )
)

# Convert C10_2012 to a factor
# In the last 12 months, have you [or your spouse/partner] taken a loan from your
# retirement account(s)? [2012 base]
NFCS.2021.Stress$C10_2012 <- factor(
  NFCS.2021.Stress$C10_2012,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert E15_2015 to a factor
# How many times have you been late with your mortgage payments in the past 12
# months? [2015 time frame]
NFCS.2021.Stress$E15_2015 <- factor(
  NFCS.2021.Stress$E15_2015,
  levels = c(1, 2, 3, 98, 99),
  labels = c("Never", "Once", "More than once", "Don't know", "Prefer not to say")
)

# Convert F2_1 to a factor
# In the past 12 months, which of the following describes your experience with
# credit cards? - I always paid my credit cards in full
NFCS.2021.Stress$F2_1 <- factor(
  NFCS.2021.Stress$F2_1,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert F2_2 to a factor
# In the past 12 months, which of the following describes your experience with
# credit cards? - In some months, I carried over a balance and was charged interest
NFCS.2021.Stress$F2_2 <- factor(
  NFCS.2021.Stress$F2_2,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert F2_3 to a factor
# In the past 12 months, which of the following describes your experience with
# credit cards? - In some months, I paid the minimum payment only
NFCS.2021.Stress$F2_3 <- factor(
  NFCS.2021.Stress$F2_3,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert F2_4 to a factor
# In the past 12 months, which of the following describes your experience with
# credit cards? - In some months, I was charged a late fee for late payment
NFCS.2021.Stress$F2_4 <- factor(
  NFCS.2021.Stress$F2_4,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert F2_5 to a factor
# In the past 12 months, which of the following describes your experience with
# credit cards? - In some months, I was charged an over the limit fee for
# exceeding my credit line
NFCS.2021.Stress$F2_5 <- factor(
  NFCS.2021.Stress$F2_5,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert F2_6 to a factor
# In the past 12 months, which of the following describes your experience with
# credit cards? - In some months, I used the cards for a cash advance
NFCS.2021.Stress$F2_6 <- factor(
  NFCS.2021.Stress$F2_6,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert P50 to a factor
# At any time in your adult life (18 and older), did your parents or grandparents
# pay for an expense of yours that was $10,000 or more?
NFCS.2021.Stress$P50 <- factor(
  NFCS.2021.Stress$P50,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert G20 to a factor
# Do you currently have any unpaid bills from a health care or medical service
# provider (e.g., a hospital, a doctor's office, or a testing lab) that are past due?
NFCS.2021.Stress$G20 <- factor(
  NFCS.2021.Stress$G20,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert G35 to a factor
# How many times have you been late with a student loan payment in the past
# 12 months?
NFCS.2021.Stress$G35 <- factor(
  NFCS.2021.Stress$G35,
  levels = c(1, 2, 3, 4, 98, 99),
  labels = c(
    "Never, payments are not due on my loans at this time",
    "Never, I have been repaying on time each month",
    "Once",
    "More than once",
    "Don't know",
    "Prefer not to say"
  )
)

# Convert G38 to a factor
# Have you been contacted by a debt collection agency in the past 12 months?
NFCS.2021.Stress$G38 <- factor(
  NFCS.2021.Stress$G38,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert H30_1 to a factor
# In the last 12 months, was there any time when you… - Did NOT fill a
# prescription for medicine because of the cost
NFCS.2021.Stress$H30_1 <- factor(
  NFCS.2021.Stress$H30_1,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert H30_2 to a factor
# In the last 12 months, was there any time when you… - SKIPPED a medical test,
# treatment or follow-up recommended by a doctor because of the cost
NFCS.2021.Stress$H30_2 <- factor(
  NFCS.2021.Stress$H30_2,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert 30_3 to a factor
# In the last 12 months, was there any time when you… - Had a medical problem
# but DID NOT go to a doctor or clinic because of the cost
NFCS.2021.Stress$H30_3 <- factor(
  NFCS.2021.Stress$H30_3,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)
