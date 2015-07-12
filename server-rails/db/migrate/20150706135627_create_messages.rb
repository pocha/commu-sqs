class CreateMessages < ActiveRecord::Migration
  def change
    create_table :messages do |t|
      t.string :app_id
      t.string :json

      t.timestamps null: false
    end
  end
end
