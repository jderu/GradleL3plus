# GradingApp

<p>An app that gives teachers the option to give grades to students and generate reports based on the given grades.</p>

<p>The app has 3 clerance levels:</p>
  <ul>
    <li>
      student:
      <ul>
        <li>can see the homeworks</li>
        <li>can see only his/hers grades</li>
        <li>can see the teachers</li>
      </ul>
    </li>
    <li>
      professor:
      <ul>
          <li>can see and add/remove homeworks</li>
          <li>can see and add/remove grades</li>
          <li>can see the teachers</li>
          <li>can generate reports</li>
      </ul>
    </li>
    <li>
      admin:
        <ul>
          <li>can see and add/remove homeworks</li>
          <li>can see and add/remove grades</li>
          <li>can see and add/remove teachers</li>
          <li>can generate reports</li>
        </ul>
      </li>
  </ul>

# Login
  <p>Based on the account you have admin/student/professor clerance.</p>

<img src="images/login1.png" width=45% hspace="20"><img src="images/login2.png" width=45% hspace="20">

# Tabs
  <p>The content is viewed as a table, if you have the clerance a double click on any of the cells of the table will make you enter editing mode, and pressing enter will make you submit the change.</p>
  <p>Remove<img src="images/remove.png" width=18>, clear the search bar<img src="images/clear_search.png" width=18>, and add<img src="images/add.png" width=18> buttons are located on the right side of the screen.</p>
  <p>Text fields have autocomplete with sugestions from a drop down list.</p>

<img src="images/homework_tab.png">
<img src="images/auto-complete.png">
<img src="images/search_bar.png">
<img src="images/reports_tab.png">

<p>Made with java, javaFX, jfoenix, and postgreSQL.</p>
