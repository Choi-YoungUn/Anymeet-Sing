module.exports ={
  devServer: {
    proxy: {
      'anymeetnsing/' :{
        target: "https://localhost:8443",
        changeOrigin: true,
        https: true
      }
    }
  }
}