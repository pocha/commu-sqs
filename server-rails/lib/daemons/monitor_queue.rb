#!/usr/bin/env ruby

# You might want to change this
ENV["RAILS_ENV"] = "development"

root = File.expand_path(File.dirname(__FILE__))
root = File.dirname(root) until File.exists?(File.join(root, 'config'))
Dir.chdir(root)

require File.join(root, "config", "environment")
# Load the app's custom environment variables here, so that they are loaded before environments/*.rb
app_environment_variables = File.join(root, 'config', 'app_environment_variables.rb')
load(app_environment_variables) if File.exists?(app_environment_variables)

$sqs = Aws::SQS::Client.new(region: ENV["AWS_region"])
require 'gcm'
$gcm = GCM.new(ENV["GCM"])

#require 'logging'

#$logger = Logging.logger(STDOUT)
#$logger.level = :info

$running = true
Signal.trap("TERM") do
  $running = false
end

while($running) do
  sleep 1
  Rails.logger.info "This daemon is still running at #{Time.now}.\n"
  begin
    queue_urls = $sqs.list_queues(queue_name_prefix: QUEUE_NAME_PREFIX).queue_urls
  rescue
    next
  end
  gcm_ids = []
  queue_urls.each do |queue_url|
    Rails.logger.info "processing queue #{queue_url}"
    message_count = $sqs.get_queue_attributes({ queue_url: queue_url, attribute_names: ["ApproximateNumberOfMessages"]}).attributes["ApproximateNumberOfMessages"].to_i
    Rails.logger.info "message count for queue #{message_count}"
    if message_count > 0
      Rails.logger.info "message count #{message_count} hence sending gcm"
      #send GCM to this app
      app_id = queue_url.split("/")[-1]
      app_id.slice! QUEUE_NAME_PREFIX 
      gcm_ids <<  App.find_by(app_id: app_id).gcm_id
    end
  end
  if !gcm_ids.empty?
      Rails.logger.info "sending gcm for gcm_ids #{gcm_ids}"
      response = $gcm.send(gcm_ids, {data: {dude: "get your ass off & start processing messages"}, collapse_key: "updated_score"})
      Rails.logger.info "response from gcm #{response}"
  end
  # Replace this with your code
  #Rails.logger.auto_flushing = true
  #$logger.error "This daemon is still running at #{Time.now}.\n"
end
