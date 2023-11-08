<?php

// make sure browsers see this page as utf-8 encoded HTML
header('Content-Type: text/html; charset=utf-8');

$limit = 10;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$results = false;

if ($query)
{
  // The Apache Solr Client library should be on the include path
  // which is usually most easily accomplished by placing in the
  // same directory as this script ( . or current directory is a default
  // php include path entry in the php.ini)
  require_once('Apache/Solr/Service.php');

  // create a new solr service instance - host, port, and webapp
  // path (all defaults in this example)
  $solr = new Apache_Solr_Service('localhost', 8983, '/solr/hw4/');

  // if magic quotes is enabled then stripslashes will be needed
  if (get_magic_quotes_gpc() == 1)
  {
    $query = stripslashes($query);
  }

  // in production code you'll always want to use a try /catch for any
  // possible exceptions emitted  by searching (i.e. connection
  // problems or a query parsing error)
  $selected = isset($_REQUEST['sort'])? $_REQUEST['sort'] : "Lucene";
  try
  {
    $additionalParameters = ($selected == "Lucene") ? array('sort' => '') : array('sort' => 'pageRankFile desc');
    $results = $solr->search($query, 0, $limit, $additionalParameters);
  }
  catch (Exception $e)
  {
    // in production you'd probably log or email this error to an admin
    // and then show a special message to the user but for this example
    // we're going to show the full exception
    die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
  }
}

?>
<html>
  <head>
    <title>PHP Solr Client</title>
  </head>
  <body>
    <h2>CSCI 572 HW4</h2>
    <form  accept-charset="utf-8" method="get">
      <label for="q">Search:</label>
      <input id="q" name="q" type="text" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>
      <input type="submit"/>
      <input type="radio" name="sort" value="pagerank" <?php if(isset($_REQUEST['sort'])&&$selected=="pagerank"){echo'checked="checked"';}?>>Page Rank
      <input type="radio" name="sort" value="Lucene" <?php if(isset($_REQUEST['sort'])&&$selected=="Lucene"){echo'checked="checked"';}?>>Lucene
    </form>
<?php
// display results
if ($results)
{
  $total = (int) $results->response->numFound;
  $start = min(1, $total);
  $end = min($limit, $total);
?>
    <div>Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:</div>
    <table style="border: 1px solid black; text-align: left">
<?php
  // iterate result documents
  $csv =  array_map('str_getcsv', file('/home/shubha/Desktop/CSCI_572_HW4/NYTIMES/URLtoHTML_nytimes_news.csv'));
  $arr = [];
  foreach ($results->response->docs as $doc)
  {
    $id = $doc->id;
    $title = $doc->title;
    if($title==""||$title==null)
    {
      $title="N/A";
    }
    $d = $doc->description;
    if($d==""||$d==null)
    {
      $d="N/A";
    }
    $id_copy = $id;
    $id = str_replace("/home/shubha/Desktop/CSCI_572_HW4/solr-7.7.3/../NYTIMES/nytimes/","",$id);
    foreach($csv as $i)
      if($id==$i[0])
      {
        $url = $i[1];
        break;
      }
    unset($i);
?>
      <tr>
        <th>TITLE : </th>
        <td ><?php echo "<a href='$url' target='_blank'>"?><?php echo htmlspecialchars($title, ENT_NOQUOTES, 'utf-8'); ?><?php echo "</a>"?></td>
      </tr>
      <tr>
        <th>URL : </th>
        <td ><?php echo "<a href='$url' target='_blank'>"?><?php echo htmlspecialchars($url, ENT_NOQUOTES, 'utf-8'); ?><?php echo "</a>"?></td>
      </tr>
      <tr>
        <th>ID : </th>
        <td ><?php echo htmlspecialchars($id_copy, ENT_NOQUOTES, 'utf-8'); ?></td>
      </tr>
      <tr>
        <th>DESCRIPTION : </th>
        <td ><?php echo htmlspecialchars($d, ENT_NOQUOTES, 'utf-8'); ?></td>
      </tr>
      <tr>
        <td><br/><hr/><br/></td><td><br/><hr/><br/></td>
      </tr>
<?php

array_push($arr,$url);
  }
?>
  </table>
<?php
echo '<script>';
echo 'console.log('. json_encode($arr, JSON_HEX_TAG) .')';
echo '</script>';
}
?>
  </body>
</html>