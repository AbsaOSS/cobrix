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

NEW_VERSION = ARGV[0]
PAGES_ROOT = File.expand_path('..', __dir__)
DOC_FOLDER = "#{PAGES_ROOT}/_docs"
VERSIONS_YAML = "#{PAGES_ROOT}/_data/versions.yaml"

unless NEW_VERSION =~ /\Av?([0-9]+\.){2}[0-9]+(-(R|r)(C|c)[1-9][0-9]*)?\Z/
  raise ArgumentError, "Version not in correct format", caller
end

latest_version = Docs.get_latest_doc_version(doc_folder: DOC_FOLDER)
Docs.create_docs(doc_folder: DOC_FOLDER,
                 new_version: NEW_VERSION,
                 latest_version: latest_version)

Docs.append_version(new_version: NEW_VERSION,
                    versions_path: VERSIONS_YAML)
