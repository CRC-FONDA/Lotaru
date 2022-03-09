#!/usr/bin/env python
# encoding: utf-8
import sys
from sklearn.metrics import r2_score
from sklearn.linear_model import BayesianRidge
import numpy

liste = sys.argv[1].replace("],[", "],[")



print(liste)
print(numpy.array(eval(liste.split()[0])))

train_X = numpy.array(numpy.array(eval(liste.split()[0])),dtype=float)
train_Y = numpy.array(sys.argv[2].split(','),dtype=float)
test_X = numpy.array(numpy.array(eval(sys.argv[3].split()[0])),dtype=float)
test_Y = numpy.array(sys.argv[4].split(','),dtype=float)

# Creating and training model
model = BayesianRidge(compute_score=True, fit_intercept=True)
model.fit(train_X, train_Y)

# Model making a prediction on test data
prediction = model.predict(test_X, return_std=True)

print(model)
# Evaluation of r2 score of the model against the test set
print('Prediction: ' + str(prediction[0]))
print('z-score ' + str((test_Y[0] - prediction[0]) / prediction[1]))