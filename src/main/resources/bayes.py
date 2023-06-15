import sys
from sklearn.metrics import r2_score
from sklearn.linear_model import BayesianRidge
from sklearn.linear_model import LinearRegression

import numpy

numpy.set_printoptions(threshold=numpy.inf)
numpy.set_printoptions(linewidth=numpy.inf)


train_X = numpy.array(sys.argv[1].split(','),dtype=float)
train_Y = numpy.array(sys.argv[2].split(','),dtype=float)
test_X = numpy.array(sys.argv[3].split(','),dtype=float)
test_Y = numpy.array(sys.argv[4].split(','),dtype=float)

# Creating and training model
model = BayesianRidge(fit_intercept=True)
model.fit(train_X.reshape(-1,1), train_Y)

# Model making a prediction on test data
prediction = model.predict(test_X.reshape(-1,1))

# Evaluation of r2 score of the model against the test set
print('Prediction: ' + str(prediction))