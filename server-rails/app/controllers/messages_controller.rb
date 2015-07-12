class MessagesController < ApplicationController
  before_action :set_message, only: [:show, :edit, :update, :destroy]

  # GET /messages
  # GET /messages.json
  def index
    #@messages = Message.all
    sqs = Aws::SQS::Client.new(region: ENV["AWS_region"])
    if ENV["RAILS_ENV"] != "production" 
      queue_urls =  sqs.list_queues(queue_name_prefix: "test-").queue_urls.each 
    else
      queue_urls =  sqs.list_queues().queue_urls.each 
    end
    @queues = []
    queue_urls.each do |queue_url|  
         puts queue_url
         queue = {}
         queue[:queue_url] = queue_url
         queue[:message_count] = sqs.get_queue_attributes({ queue_url: queue_url, attribute_names: ["ApproximateNumberOfMessages"]}).attributes["ApproximateNumberOfMessages"]
         queue[:last_message] = sqs.receive_message({ queue_url: queue_url, max_number_of_messages: 1}).messages
         queue[:last_message] = queue[:last_message][0].nil? ? "-" : queue[:last_message][0].body
         @queues << queue
    end
  end

  # GET /messages/1
  # GET /messages/1.json
  def show
  end

  # GET /messages/new
  def new
    @message = Message.new
  end

  # GET /messages/1/edit
  def edit
  end

  # POST /messages
  # POST /messages.json
  def create

    @message = Message.new(message_params)

    if @message.save
      #format.html { redirect_to @message, notice: 'Message was successfully created.' }
      #format.json { render :show, status: :created, location: @message }
      #puts message_params
      redirect_to deliver_path(app_id: message_params[:app_id], json: message_params[:json]), notice: 'Message is successfully created'
    else
      respond_to do |format|
        format.html { render :new }
        format.json { render json: @message.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /messages/1
  # PATCH/PUT /messages/1.json
  def update
    respond_to do |format|
      if @message.update(message_params)
        format.html { redirect_to @message, notice: 'Message was successfully updated.' }
        format.json { render :show, status: :ok, location: @message }
      else
        format.html { render :edit }
        format.json { render json: @message.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /messages/1
  # DELETE /messages/1.json
  def destroy
    @message.destroy
    respond_to do |format|
      format.html { redirect_to messages_url, notice: 'Message was successfully destroyed.' }
      format.json { head :no_content }
    end
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_message
      @message = Message.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def message_params
      params.require(:message).permit(:app_id, :json)
    end
end
