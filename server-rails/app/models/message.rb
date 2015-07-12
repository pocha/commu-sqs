class Message < ActiveRecord::Base
  def self.delete_queues
    sqs = Aws::SQS::Client.new(region: ENV["AWS_region"])
    queue_urls = sqs.list_queues(queue_name_prefix: QUEUE_NAME_PREFIX).queue_urls
    queue_urls.each do |queue_url|
      sqs.delete_queue(
          queue_url: queue_url
      )
    end
  end
end
