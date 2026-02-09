from app import create_app, db

app = create_app()

if __name__ == '__main__':
    # Creates tables if they don't exist
    with app.app_context():
        db.create_all()
    #app.run(debug=True, port=5000)
    app.run(host='0.0.0.0', port=5000, debug=True)