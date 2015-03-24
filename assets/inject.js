<script type="text/javascript">
    function fillAccount() {
        loginName.value = '%uname%';
        loginPassword.value = '%upassword%';
    }
    
    function saveAccountInfoJS() {
        window.JSINTERFACE.saveAccountInfo(loginName.value, loginPassword.value);
    }
    
    function doAutoLogIn() {
        loginApp.doLogin();
    }
</script>