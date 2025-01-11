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
# Splitting dataset in the subsets: demographics, financial capabilities and
# financial stress

#
# Financial Capabilities Features
#
CapabilitiesFeatures <- c(
  'NFCSID',
  'B1',
  'B2',
  'B31',
  'B42',
  'B43',
  'C1_2012',
  'C2_2012',
  'C5_2012',
  'B14',
  'EA_1',
  'E7',
  'F1',
  'H1',
  'M1_1',
  'M4',
  'M20'
)
str(CapabilitiesFeatures)
NFCS2021Capabilities <- NFCS2021[, CapabilitiesFeatures]
str(CapabilitiesFeatures)

#
# Financial Capabilities Features
#
# Convert B1 to a factor
# Do you [Does your household] have a checking account?
NFCS2021Capabilities$B1 <- factor(
  NFCS2021Capabilities$B1,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert B2 to a factor
# Do you [Does your household] have a savings account, money market account, or CDs?
NFCS2021Capabilities$B2 <- factor(
  NFCS2021Capabilities$B2,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert B31 to a factor
# How often do you use your mobile phone to pay for a product or service in
# person at a store, gas station, or restaurant (e.g., by waving/tapping your
# mobile phone over a sensor at checkout, scanning a barcode or QR code using
# your mobile phone, or using so
NFCS2021Capabilities$B31 <- factor(
  NFCS2021Capabilities$B31,
  levels = c(1, 2, 3, 98, 99),
  labels = c(
    "Frequently",
    "Sometimes",
    "Never",
    "Don't know",
    "Prefer not to say"
  )
)

# Convert B42 to a factor
# How often do you use your mobile phone to transfer money to another person?
NFCS2021Capabilities$B42 <- factor(
  NFCS2021Capabilities$B42,
  levels = c(1, 2, 3, 98, 99),
  labels = c(
    "Frequently",
    "Sometimes",
    "Never",
    "Don't know",
    "Prefer not to say"
  )
)

# Convert B43 to a factor
# How often do you use websites or apps to help with financial tasks such as
# budgeting, saving, or credit management (e.g., GoodBudget, Mint, Credit Karma,
# etc.)? Please do not include websites or apps for making payments or money transfers.

NFCS2021Capabilities$B43 <- factor(
  NFCS2021Capabilities$B43,
  levels = c(1, 2, 3, 98, 99),
  labels = c(
    "Frequently",
    "Sometimes",
    "Never",
    "Don't know",
    "Prefer not to say"
  )
)

# Convert C1_2012 to a factor
# Do you [or your spouse/partner] have any retirement plans through a current or
# previous employer, like a pension plan, [a Thrift Savings Plan (TSP),] or a
# 401(k)? [2012 base]
NFCS2021Capabilities$C1_2012 <- factor(
  NFCS2021Capabilities$C1_2012,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert C2_2012 to a factor
# Were these plans provided by your employer or your [spouse's/partner's]
# employer, or both? [2012 base]
NFCS2021Capabilities$C2_2012 <- factor(
  NFCS2021Capabilities$C2_2012,
  levels = c(1, 2, 3, 98, 99),
  labels = c(
    "Your employer",
    "Your [spouse's/partner's] employer",
    "Both your employer and your [spouse's/partner's] employer",
    "Don't know",
    "Prefer not to say"
  )
)


# Convert C5_2012 to a factor
# Do you [or your spouse/partner] regularly contribute to a retirement account
# like a [Thrift Savings Plan (TSP),] 401(k) or IRA? [2012 base]
NFCS2021Capabilities$C5_2012 <- factor(
  NFCS2021Capabilities$C5_2012,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert B14 to a factor
# Not including retirement accounts, do you [does your household] have any
# investments in stocks,
#bonds, mutual funds, or other securities?
NFCS2021Capabilities$B14 <- factor(
  NFCS2021Capabilities$B14,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert EA_1 to a factor
# Do you [or your spouse/partner] currently own your home?
NFCS2021Capabilities$EA_1 <- factor(
  NFCS2021Capabilities$EA_1,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert E7 to a factor
# Do you currently have any mortgages on your home?
NFCS2021Capabilities$E7 <- factor(
  NFCS2021Capabilities$E7,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert F1 to a factor
# How many credit cards do you have?
NFCS2021Capabilities$F1 <- factor(
  NFCS2021Capabilities$F1,
  levels = c(1, 2, 3, 4, 5, 6, 7, 98, 99),
  labels = c(
    "1",
    "2 to 3",
    "4 to 8",
    "9 to 12",
    "13 to 20",
    "More than 20",
    "No credit cards",
    "Don't know",
    "Prefer not to say"
  )
)

# Convert H1 to a factor
# Are you covered by health insurance?
NFCS2021Capabilities$H1 <- factor(
  NFCS2021Capabilities$H1,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert M1_1 to a factor
# How strongly do you agree or disagree with the following statements? - I am
# good at dealing with day-to-day financial matters, such as checking accounts,
# credit and debit cards, and tracking expenses
NFCS2021Capabilities$M1_1 <- factor(
  NFCS2021Capabilities$M1_1,
  levels = c(1, 2, 3, 4, 5, 6, 7, 98, 99),
  labels = c(
    "1 - Strongly Disagree",
    "2",
    "3",
    "4 - Neither Agree nor Disagree",
    "5",
    "6",
    "7 - Strongly Agree",
    "Don't know",
    "Prefer not to say"
  )
)

# Convert M4 to a factor
# On a scale from 1 to 7, where 1 means very low and 7 means very high, how would
# you assess your overall financial knowledge?
NFCS2021Capabilities$M4 <- factor(
  NFCS2021Capabilities$M4,
  levels = c(1, 2, 3, 4, 5, 6, 7, 98, 99),
  labels = c(
    "1 - Very Low",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7 - Very High",
    "Don't know",
    "Prefer not to say"
  )
)

# Convert M20 to a factor
# Was financial education offered by a school or college you attended, or a
# workplace where you were employed?
NFCS2021Capabilities$M20 <- factor(
  NFCS2021Capabilities$M20,
  levels = c(1, 2, 3, 98, 99),
  labels = c(
    "Yes, but I did not participate in the financial education of",
    "Yes, and I did participate in the financial education",
    "No",
    "Don't know",
    "Prefer not to say"
  )
)
