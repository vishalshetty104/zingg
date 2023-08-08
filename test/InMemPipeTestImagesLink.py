#  Zingg
#  Copyright (C) 2021-Present  Zingg Labs,inc
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU Affero General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU Affero General Public License for more details.
#
#  You should have received a copy of the GNU Affero General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

from zingg.client import *
from zingg.pipes import *
from pyspark.sql.types import *
import pandas

import pyspark.sql.functions as fn
from sentence_transformers import SentenceTransformer, util
import torch
import pickle

from PIL import Image

df = (getSparkSession().read.json('/home/ubuntu/image_data/listings/metadata'))

df = (
  df
    .filter("country='US'")
    .select(
      'item_id',
      'brand',
      'bullet_point',
      'domain_name',
      'marketplace',
      'item_keywords',
      'item_name',
      'product_description',
      'main_image_id',
      'other_image_id',
      'node'
    )
  )

image_metadata = (
  getSparkSession()
    .read
    .csv(
      path='/home/ubuntu/image_data/images/metadata',
      sep=',',
      header=True,
      )
    )

@fn.udf(ArrayType(StringType()))
def get_english_values_from_array(array=None):

   # prioritized list of english language codes (biased towards us english)
  english = ['en_US','en_CA','en_GB','en_AU','en_IN','en_SG','en_AE']

  # initialize search 
  values = []
  if array is None: array=[]

  # for each potential english code
  for e in english:

    # for each item in array
    for a in array:
      # if we found the english variant we want
      if a['language_tag']==e:
        # get value and stop
        values += [a['value']]

    # if value has been found, then break
    if len(values) > 0: break

  return values

model = SentenceTransformer('clip-ViT-B-32',device='cuda')

@fn.udf(ArrayType(DoubleType()))
#@fn.udf(StringType())
def get_image_embedding(path):

  embedding = []

  if path is not None:

    full_path = '/home/ubuntu/image_data/images/small/' + path

    # open image and convert to embedding
    try:
      image = Image.open(full_path).convert('RGB')
      embedding = model.encode(image, batch_size=128, convert_to_tensor=False, show_progress_bar=False)
      embedding = embedding.tolist()
    except:
      pass

  # return embedding value
  return embedding

items = (
  df
    .alias('a')
    .select(
      'item_id',
      'domain_name',
      'marketplace',
      get_english_values_from_array('brand')[0].alias('brand'),
      get_english_values_from_array('item_name')[0].alias('item_name'),
      get_english_values_from_array('product_description')[0].alias('product_description'),
      get_english_values_from_array('bullet_point').alias('bulletpoint'),
      get_english_values_from_array('item_keywords').alias('item_keywords'),
      fn.split( fn.col('node')[0]['node_name'], '/').alias('hierarchy'),
      'main_image_id'
      )
    .join(
      image_metadata.alias('b').select('image_id','path'),
      on=fn.expr('a.main_image_id=b.image_id'),
      how='left'
      )
    .withColumn('main_image_embedding', get_image_embedding(fn.col('path')))
    .drop('main_image_id','image_id','bulletpoint','item_keywords','hierarchy')
  )

#build the arguments for zingg
args = Arguments()
#set field definitions
item_id = FieldDefinition("item_id", "string", MatchType.DONT_USE)
domain_name = FieldDefinition("domain_name", "string", MatchType.DONT_USE)
marketplace = FieldDefinition("marketplace", "string", MatchType.DONT_USE)
brand = FieldDefinition("brand","string", MatchType.FUZZY)
item_name = FieldDefinition("item_name", "string", MatchType.TEXT)
product_description = FieldDefinition("product_description", "string", MatchType.DONT_USE)
path = FieldDefinition("path", "string", MatchType.DONT_USE)
main_image_embedding = FieldDefinition("main_image_embedding", "array<double>", MatchType.FUZZY)

#fieldDefs = [item_id, domain_name, marketplace, brand, item_name,product_description, bulletpoint, item_keywords, hierarchy,path, main_image_embedding]
fieldDefs = [item_id, domain_name, marketplace, brand, item_name,product_description,path,main_image_embedding]
args.setFieldDefinition(fieldDefs)
#set the modelid and the zingg dir
args.setModelId("9999")
args.setZinggDir("/tmp/modelSmallImages")
args.setNumPartitions(16)
args.setLabelDataSampleSize(0.2)

items1 = items.limit(100)
items2 = items1.limit(10)

inputPipeSmallImages1=InMemoryPipe("smallImages1")
inputPipeSmallImages1.setDataset(items1)

inputPipeSmallImages2=InMemoryPipe("smallImages2")
inputPipeSmallImages2.setDataset(items2)

args.setData(inputPipeSmallImages1,inputPipeSmallImages2)

#setting outputpipe in 'args'
outputPipe = Pipe("resultSmallImages", "parquet")
outputPipe.addProperty("location", "/tmp/resultSmallImages")
args.setOutput(outputPipe)

options = ClientOptions([ClientOptions.PHASE,"link"])

#Zingg execution for the given phase
zingg = Zingg(args, options)
zingg.initAndExecute()


