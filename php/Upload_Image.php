<?php
/**
 * @package GoodHikes server-side
 * @author Chelsea
 * @copyright (C) 2016 - Team Magic
 * @license GNU/GPLv3 http://www.gnu.org/licenses/gpl-3.0.html
**/

require_once 'include/DB_Functions.php';
$db = new DB_Functions();
 
// json response array
$response = array("error" => FALSE);
 
    if (isset($_POST['uid']) && isset($_POST['image_str'])) {
        $uid = $_POST['uid'];
        $image_str = $_POST['image_str'];
        if ($db->storeImage($uid, $image_str)) {
    	    $response["error"] = FALSE;
        } else {
        	$response["error"] = TRUE;
        }
    } else {
        $response['error'] = TRUE;
    }
    
    echo json_encode($response);
 
?>