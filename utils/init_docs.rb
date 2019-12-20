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

PAGES_ROOT = File.expand_path('..', __dir__)
JEKYLL_CONFIG = "#{PAGES_ROOT}/_config.yml"
INDEX_PAGE = "#{PAGES_ROOT}/index.html"
DOC_FOLDER = "#{PAGES_ROOT}/_docs"
VERSIONS_YAML = "#{PAGES_ROOT}/_data/versions.yaml"

DESC_PLACEHOLDER = '#{PROJECT_DESCRIPTION}'
NAME_PLACEHOLDER = '#{PROJECT_NAME}'

ACTUAL_NAME = ARGV[0]
ACTUAL_DECS = ARGV[1]
STARTING_VERSION = ARGV[2] || '0.1.0'

config_yaml = File.read(JEKYLL_CONFIG)
new_config = config_yaml
  .gsub(NAME_PLACEHOLDER, ACTUAL_NAME)
  .gsub(DESC_PLACEHOLDER, ACTUAL_DECS)
File.write(JEKYLL_CONFIG, new_config)

index_page =  File.read(INDEX_PAGE)
new_index_page = index_page
  .gsub(NAME_PLACEHOLDER, ACTUAL_NAME)
  .gsub(DESC_PLACEHOLDER, ACTUAL_DECS)
File.write(INDEX_PAGE, new_index_page)

if STARTING_VERSION != '0.1.0'
  Docs.create_docs(doc_folder: DOC_FOLDER,
                   new_version: STARTING_VERSION,
                   latest_version: '0.1.0')
  Docs.reset_version_list(versions_path: VERSIONS_YAML)
  Docs.append_version(new_version: STARTING_VERSION,
                      versions_path: VERSIONS_YAML)
  Docs.remove_docs(doc_folder: DOC_FOLDER, version: '0.1.0')
end
