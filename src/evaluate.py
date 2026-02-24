import tensorflow as tf
from preprocess import *

model = tf.keras.models.load_model("../models/saved_model")

texts, labels = load_data("../data/sample_dataset.csv")

tokenizer = build_tokenizer(texts)
X = tokenize(tokenizer, texts)

loss, acc, precision, recall = model.evaluate(X, labels)

print("Accuracy:", acc)
print("Precision:", precision)
print("Recall:", recall)
