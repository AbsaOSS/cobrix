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

class Release
  attr_reader :id, :title, :description, :issues

  def post_init
    get_issues
    open_issues = @issues.select { |issue| issue.state == 'open' }
    if OptParser.options.strict && open_issues.size > 0
      raise StandardError, "Release has open issues #{open_issues.map(&:number)}", caller
    end
  end

  def initialize(id:, title:, description:)
    @id = id
    @title = title
    @description = description
  end

  def get_issues(issues:)
    @issues = issues.inject([]) { |acc, v| acc << Issue.new(number: v[:number], title: v[:title]) }
    @issues
  end
end

class ZenhubRelease < Release
  def self.create(args)
    release = ZenhubRelease.new(id: args[:release_id],
                                title: args[:title],
                                description: args[:description])
    release.post_init
    release
  end

  def get_issues
    return @issues unless @issues.nil?
    issues = call(URI("#{OptParser.options.zenhub_url}/p1/reports/release/#{id}/issues"))
    super(issues: issues)
  end
end

class GithubRelease < Release
  def self.create(args)
    puts "WARN You have #{args[:open_issues]} issues open" if args[:open_issues] > 0
    release = GithubRelease.new(id: args[:number],
                                title: args[:title],
                                description: args[:description])
    release.post_init
    release
  end

  def get_issues
    return @issues unless @issues.nil?
    issues_with_prs = call(URI("#{OptParser.options.github_url}/issues?milestone=#{id}&state=all"))
    issues = issues_with_prs.select { |issue| issue[:pull_request].nil? }
    super(issues: issues)
  end
end
