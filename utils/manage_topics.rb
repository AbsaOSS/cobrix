# Copyright 2018-2019 ABSA Group Limited
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

require_relative 'create_docs/docs'
require 'yaml'

PAGES_ROOT = File.expand_path('..', __dir__)
DOC_FOLDER = "#{PAGES_ROOT}/_docs"
MENU_DATA = "#{PAGES_ROOT}/_data/topics.yaml"

ACTION = ARGV[0]
TOPIC_NAME = ARGV[1]

if ACTION == 'remove'
  Docs.remove_topic(topic_name: TOPIC_NAME,
                    doc_folder: DOC_FOLDER,
                    yaml_path: MENU_DATA)
elsif ACTION == 'add'
  Docs.add_topic(topic_name: TOPIC_NAME,
                    doc_folder: DOC_FOLDER,
                    yaml_path: MENU_DATA)
else
  puts "Action can be either remove or add"
end
