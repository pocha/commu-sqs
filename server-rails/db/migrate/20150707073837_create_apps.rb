class CreateApps < ActiveRecord::Migration
  def change
    create_table :apps do |t|
      t.string :app_id
      t.text :gcm_id

      t.timestamps null: false
    end
  end
end
