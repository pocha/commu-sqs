class EndpointsController < ApplicationController
  def deliver
    sqs = Aws::SQS::Client.new(region: ENV["AWS_region"])
    queue_name = QUEUE_NAME_PREFIX + params[:app_id]
    queue_url = sqs.get_queue_url(queue_name: queue_name).queue_url
    sqs.send_message(
      queue_url: queue_url,
      message_body: params[:json]
    )
    redirect_to root_path, notice: "message successfully pushed to queue #{queue_name}"
  end

  def register
    app = App.find_or_create_by(app_id: params[:app_id]) 
    app.update_attributes(gcm_id: params[:gcm_id])
    #create new queue with app_id as name
    sqs = Aws::SQS::Client.new(region: ENV["AWS_region"])
    queue_name = QUEUE_NAME_PREFIX + params[:app_id]
    queue = sqs.create_queue(queue_name: queue_name)
  end

end
