<?php
$targetPath = "uploads/";
 
$response = array();

$FileURL = "http://".gethostbyname(gethostname())."/KrystianKl/API/v1/".$targetPath;

if(!is_dir($targetPath)) mkdir($targetPath);
 
if(isset($_FILES["UImage"]["name"])) {
    $targetPath = $targetPath.basename($_FILES["UImage"]["name"]);
 
    $UserID = isset($_POST["UserID"]) ? $_POST["UserID"] : "";
    $EMail = isset($_POST["EMail"]) ? $_POST["EMail"] : "";
 
    $response["file_name"] = basename($_FILES["UImage"]["name"]);
    $response["UserID"] = $UserID;
    $response["EMail"] = $EMail;
 
    try {
        if (!move_uploaded_file($_FILES["UImage"]["tmp_name"], $targetPath)) {
            $response["error"] = true;
            $response["message"] = "Nie można przestawić pliku!";
        }
 
        $response["message"] = "Pomyślnie wgrano plik!";
        $response["error"] = false;
        $response["file_path"] = $FileURL.basename($_FILES["UImage"]["name"]);
    } catch (Exception $e) {
        $response["error"] = true;
        $response["message"] = $e->getMessage();
    }
} else {
    $response["error"] = true;
    $response["message"] = "Nie przesłano żadnego pliku!";
}
 
echo json_encode($response);
?>
